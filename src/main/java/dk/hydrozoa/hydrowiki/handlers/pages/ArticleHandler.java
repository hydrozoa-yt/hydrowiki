package dk.hydrozoa.hydrowiki.handlers.pages;

import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.Templater;
import dk.hydrozoa.hydrowiki.Util;
import dk.hydrozoa.hydrowiki.database.Counter;
import dk.hydrozoa.hydrowiki.database.DbArticles;
import dk.hydrozoa.hydrowiki.database.DbMedia;
import dk.hydrozoa.hydrowiki.database.DbUsers;
import dk.hydrozoa.hydrowiki.handlers.IHandler;
import dk.hydrozoa.hydrowiki.model.InfoMessage;
import dk.hydrozoa.hydrowiki.parser.FlexmarkParser;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ArticleHandler extends IHandler {

    private FlexmarkParser parser;

    private String S3_URL;

    public ArticleHandler(ServerContext ctx) {
        super(ctx);
        S3_URL = ctx.getProperties().getProperty("s3.public_access");
        parser = new FlexmarkParser(S3_URL);
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        String path = request.getHttpURI().getDecodedPath();
        String pathSlashesRemoved = Util.removeTrailingSlashes(Util.removeLeadingSlashes(path));
        String[] pathTokens = pathSlashesRemoved.split("/");

        if (pathTokens.length < 2) {
            Response.sendRedirect(request, response, callback, "/");
            return true;
        }

        switch (request.getMethod()) {
            case "POST":
                return handlePost(pathTokens, request, response, callback);
            case "GET":
            default:
                return handleGet("", pathTokens, request, response, callback);
        }
    }

    // todo change infoMessage to be attribute instead
    // todo split this behemoth into seperate methods
    private boolean handleGet(String infoMessage, String[] pathTokens, Request request, Response response, Callback callback) {
        DbUsers.RUser loggedIn = getLoggedIn(request);

        String articleName = pathTokens[1].replace("_", " ");

        if (articleName.startsWith("media:")) { // display media version of article
            return sendMediaArticle(loggedIn, articleName, request, response, callback);
        }

        DbArticles.RArticle article = null;
        try (Connection con = getContext().getDBConnectionPool().getConnection()) {
            article = DbArticles.getArticle(articleName, con, getDatabaseLookupCounter());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (article == null) {
            return sendNoArticleFound(articleName, request, response, callback);
        }

        Fields fields = Request.extractQueryParameters(request);
        if (fields != null) {
            String action = fields.getValue("action");

            if (action != null && action.equalsIgnoreCase("delete")) {
                if (loggedIn.rights() > 0) {
                    try (Connection con = getContext().getDBConnectionPool().getConnection()) {
                        DbArticles.deleteArticle(article.id(), con, new Counter());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    // todo handle infomessage in "/all" handler
                    InfoMessage.Message errorMessage = new InfoMessage.Message(InfoMessage.TYPE.SUCCESS, "Deleted article "+article.title());
                    request.getSession(true).setAttribute("infoMessage", errorMessage);
                    Response.sendRedirect(request, response, callback, "/all/");
                }
            }

            // todo dont have to iterate over the fields multiple times, should just be once
            if (fields.stream().anyMatch(p-> p.getName().equalsIgnoreCase("action") && p.getValue().equalsIgnoreCase("edit"))) {
                // display article for editing
                Map model = Map.of(
                        "loggedIn", loggedIn != null,
                        "articleName", article.title(),
                        "articleNameHumanReadable", article.title().replace("_", " "),
                        "articleContentCode", article.content(),
                        "infoMessage", infoMessage
                );

                String content = Templater.renderTemplate("article/article_edit.ftl", model);
                String fullPage = Templater.renderBaseTemplate(request, articleName, content);
                sendHtml(200, fullPage, response, callback);
                return true;
            }
            if (fields.stream().anyMatch(p-> p.getName().equalsIgnoreCase("action") && p.getValue().equalsIgnoreCase("history"))) {
                // display article history
                List<Map> history = new ArrayList<>();
                try (Connection con = getContext().getDBConnectionPool().getConnection()) {
                    List<DbArticles.RArticleEditWithExtra> edits = DbArticles.getAllArticleEdits(article.id(), con, getDatabaseLookupCounter());
                    edits.forEach(editWithExtra -> {
                        Map model = Map.of(
                                "timestamp", editWithExtra.edit().created().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm")),
                                "author", editWithExtra.username(),
                                "version", editWithExtra.edit().version(),
                                "text", "Version "+editWithExtra.edit().version(),
                                "charLenDiff", editWithExtra.edit().charLenDiff()
                        );
                        history.add(model);
                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                Map model = Map.of(
                        "articleNameHumanReadable", article.title().replace("_", " "),
                        "articleName", article.title(),
                        "history", history
                );

                String content = Templater.renderTemplate("article/article_history.ftl", model);
                String fullPage = Templater.renderBaseTemplate(request, articleName, content);
                sendHtml(200, fullPage, response, callback);
                return true;
            }
            if (fields.stream().anyMatch(p-> p.getName().equalsIgnoreCase("diff") && p.getValue().equalsIgnoreCase("prev"))) {
                // display article version compared with previous version
                if (fields.getValue("id") == null) {
                    return false;
                }
                int versionNumber = fields.get("id").getValueAsInt();

                // get all edits between the newest and the requested
                List<DbArticles.RArticleEdit> edits = null;
                try (Connection con = getContext().getDBConnectionPool().getConnection()) {
                    edits = DbArticles.getArticleEditsSince(article.id(), versionNumber, con, getDatabaseLookupCounter());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }


                String[] diffLines = null;
                Patch<String> patch = null;

                List<String> latest = Arrays.asList(article.content().split("\n"));
                List<String> previous = null;
                for (int i = 0; i < edits.size(); i++) {
                    if (i > 0) { // not first iteration
                        latest = previous;
                    }
                    diffLines = edits.get(i).diff().split("\n");
                    patch = UnifiedDiffUtils.parseUnifiedDiff(Arrays.asList(diffLines));
                    try {
                        previous = patch.applyTo(latest);
                    } catch (PatchFailedException e) {
                        throw new RuntimeException(e);
                    }
                }

                DiffRowGenerator generator = DiffRowGenerator.create()
                        .showInlineDiffs(true)
                        .mergeOriginalRevised(true)
                        .inlineDiffByWord(true)
                        .oldTag(f -> f ? "<s>" : "</s>")      //introduce markdown style for strikethrough
                        .newTag(f -> f? "<b>" : "</b>")     //introduce markdown style for bold
                        .build();

                List<DiffRow> rows = generator.generateDiffRows(
                        previous,
                        latest);

                StringBuilder diffBuilder = new StringBuilder();
                rows.forEach(row -> {
                    diffBuilder.append(row.getOldLine());
                    diffBuilder.append("<br />");
                });

                Map model = Map.of(
                        "articleNameHumanReadable", article.title().replace("_", " "),
                        "articleName", article.title(),
                        "version", versionNumber,
                        "changes", diffBuilder.toString()
                );

                String content = Templater.renderTemplate("article/article_diff.ftl", model);
                String fullPage = Templater.renderBaseTemplate(request, articleName, content);
                sendHtml(200, fullPage, response, callback);
                return true;
            }
        }

        return sendArticle(loggedIn, article, request, response, callback);
    }

    /**
     * Sends a normal article with name and content text. This is the default view of an article.
     */
    private boolean sendArticle(DbUsers.RUser loggedIn, DbArticles.RArticle article, Request request, Response response, Callback callback) {
        // display article for reading
        Map model = Map.of(
                "loggedIn", getLoggedIn(request) != null,
                "articleName", article.title(),
                "articleNameHumanReadable", article.title().replace("_", " "),
                "articleContent", parser.parse(article.content())
        );

        String content = Templater.renderTemplate("article/article.ftl", model);
        String fullPage = Templater.renderBaseTemplate(request, article.title(), content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }

    /**
     * Sends article for a media.
     * This will contain the name, show the media, some info and option to delete.
     */
    private boolean sendMediaArticle(DbUsers.RUser loggedIn, String articleName, Request request, Response response, Callback callback) {
        DbMedia.RMedia media;
        String authorUsername = null;
        try (Connection con = getContext().getDBConnectionPool().getConnection()) {
            media = DbMedia.getMedia(articleName.replace("media:", ""), con, new Counter());
            authorUsername = DbUsers.getUser(media.userId(), con, new Counter()).username();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // check if fields where we should act
        Fields fields = Request.extractQueryParameters(request);
        String actionField = fields.getValue("action");
        if (loggedIn != null && actionField != null) {
            if (actionField.equalsIgnoreCase("delete") && loggedIn.rights() >= 1) {
                // todo test if this works
                getContext().getS3Interactor().deleteFile(media.filename());
                try (Connection con = getContext().getDBConnectionPool().getConnection()) {
                    DbMedia.deleteMedia(media.filename(), con, new Counter());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                InfoMessage.Message errorMessage = new InfoMessage.Message(InfoMessage.TYPE.SUCCESS, "Deleted media "+media.filename());
                request.getSession(true).setAttribute("infoMessage", errorMessage);
                Response.sendRedirect(request, response, callback, "/media/");
                return true;
            }
        }

        Map model = Map.of(
                "mediaUrl", S3_URL+"/"+media.filename(),
                "articleName", articleName,
                "loggedIn", loggedIn != null,
                "user", authorUsername,
                "creation", media.created().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );

        String content = Templater.renderTemplate("article/article_media.ftl", model);
        String fullPage = Templater.renderBaseTemplate(request, articleName, content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }

    private boolean sendNoArticleFound(String articleName, Request request, Response response, Callback callback) {
        boolean isLoggedIn = getLoggedIn(request) != null;
        Map model = Map.of(
                "isLoggedIn", isLoggedIn,
                "articleName", articleName,
                "articleNameHumanReadable", articleName
        );
        String content = Templater.renderTemplate("article/article_none_found.ftl", model);
        String fullPage = Templater.renderBaseTemplate(request, articleName, content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }

    private boolean handlePost(String[] pathTokens, Request request, Response response, Callback callback) {
        DbUsers.RUser user = getLoggedIn(request);
        if (user == null) {
            return false;
        }

        // find relevant article from url
        String articleName = pathTokens[1].replace("_", " ");

        DbArticles.RArticle article = null;
        try (Connection con = getContext().getDBConnectionPool().getConnection()) {
            article = DbArticles.getArticle(articleName, con, getDatabaseLookupCounter());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // do not handle if no article is found
        if (article == null) {
            return false;
        }

        try {
            Fields fields = Request.getParameters(request);
            if (fields != null) {
                if (fields.stream().anyMatch(p-> p.getName().equalsIgnoreCase("action") && p.getValue().equalsIgnoreCase("edit"))) {
                    String newContent = fields.getValue("articleContent");
                    try (Connection con = getContext().getDBConnectionPool().getConnection()) {
                        DbArticles.updateArticleContent(article.id(), newContent, con, getDatabaseLookupCounter());
                        String diffToPrevious = Util.generateDiffs(article.title(), newContent, article.content());
                        int charDiff = newContent.length() - article.content().length();
                        DbArticles.insertArticleEdit(user.id(), article.id(), diffToPrevious, charDiff, con, getDatabaseLookupCounter());
                    }

                    String info = "Saved new version successfully";
                    return handleGet(info, pathTokens, request, response, callback);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
