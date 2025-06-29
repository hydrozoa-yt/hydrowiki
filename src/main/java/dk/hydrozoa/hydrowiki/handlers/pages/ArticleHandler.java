package dk.hydrozoa.hydrowiki.handlers.pages;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRowGenerator;
import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.Templater;
import dk.hydrozoa.hydrowiki.Util;
import dk.hydrozoa.hydrowiki.database.DbArticles;
import dk.hydrozoa.hydrowiki.handlers.IHandler;
import dk.hydrozoa.hydrowiki.ui.WikiTextParser;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ArticleHandler extends IHandler {

    private WikiTextParser parser;

    public ArticleHandler(ServerContext ctx) {
        super(ctx);
        parser = new WikiTextParser();
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

    private boolean handleGet(String infoMessage, String[] pathTokens, Request request, Response response, Callback callback) {
        String articleName = pathTokens[1];

        DbArticles.RArticle article = null;
        try (Connection con = getContext().getDBConnectionPool().getConnection()) {
            article = DbArticles.getArticle(articleName, con, getDatabaseLookupCounter());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (article == null) {
            return false;
        }

        Fields fields = Request.extractQueryParameters(request); // todo use getQueryParameters instead, as it can't throw an exception
        if (fields != null) {
            // todo dont have to iterate over the fields multiple times, should just be once
            if (fields.stream().anyMatch(p-> p.getName().equalsIgnoreCase("action") && p.getValue().equalsIgnoreCase("edit"))) {
                // display article for editing
                Map model = Map.of(
                        "articleName", article.title(),
                        "articleContentCode", article.content(),
                        "infoMessage", infoMessage
                );

                String content = Templater.renderTemplate("article_edit.ftl", model);
                String fullPage = Templater.renderBaseTemplate(articleName, content);
                sendHtml(200, fullPage, response, callback);
                return true;
            }
            if (fields.stream().anyMatch(p-> p.getName().equalsIgnoreCase("action") && p.getValue().equalsIgnoreCase("history"))) {
                // display article history
                List<String> history = new ArrayList<>();
                try (Connection con = getContext().getDBConnectionPool().getConnection()) {
                    List<DbArticles.RArticleEdit> edits = DbArticles.getAllArticleEdits(con, getDatabaseLookupCounter());
                    edits.forEach(edit -> history.add("Version "+edit.version()));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                Map model = Map.of(
                        "articleName", article.title(),
                        "history", history
                );

                String content = Templater.renderTemplate("article_history.ftl", model);
                String fullPage = Templater.renderBaseTemplate(articleName, content);
                sendHtml(200, fullPage, response, callback);
                return true;
            }
            if (fields.stream().anyMatch(p-> p.getName().equalsIgnoreCase("diff") && p.getValue().equalsIgnoreCase("prev"))) {
                // display article version compared with previous version

                if (fields.getValue("id") == null) {
                    return false;
                }
                int versionNumber = fields.get("id").getValueAsInt();

                DbArticles.RArticleEdit edit = null;
                try (Connection con = getContext().getDBConnectionPool().getConnection()) {
                    edit = DbArticles.getArticleEdit(article.id(), versionNumber, con, getDatabaseLookupCounter());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                // todo get newest version and then get all edits in between the requested and the newest version.
                // todo then apply the patches sequentially until the requested version is reached.
                // todo then apply the patch from the requested version to recreate the lines of the previous version
                // todo then create a DiffRowGenerator to generate diffrows for the diff between the versions and display in human readable form

                //DiffRowGenerator gen = DiffRowGenerator.create().build();
                //gen.generateDiffRows()


                /*List<String> history = new ArrayList<>();
                try (Connection con = getContext().getDBConnectionPool().getConnection()) {
                    List<DbArticles.RArticleEdit> edits = DbArticles.getAllArticleEdits(con, getDatabaseLookupCounter());
                    edits.forEach(edit -> history.add("Version "+edit.version()));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                Map model = Map.of(
                        "articleName", article.title(),
                        "history", history
                );

                String content = Templater.renderTemplate("article_history.ftl", model);
                String fullPage = Templater.renderBaseTemplate(articleName, content);
                sendHtml(200, fullPage, response, callback);
                return true;*/
            }
        }

        // display article for reading
        Map model = Map.of(
                "articleName", article.title(),
                "articleContent", parser.parse(article.content())
        );

        String content = Templater.renderTemplate("article.ftl", model);
        String fullPage = Templater.renderBaseTemplate(articleName, content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }

    private boolean handlePost(String[] pathTokens, Request request, Response response, Callback callback) {
        // find relevant article from url
        String articleName = pathTokens[1];

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
                        DbArticles.insertArticleEdit(article.id(), diffToPrevious, con, getDatabaseLookupCounter());
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
