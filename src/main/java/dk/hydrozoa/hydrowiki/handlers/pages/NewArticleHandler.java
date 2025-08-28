package dk.hydrozoa.hydrowiki.handlers.pages;

import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.Templater;
import dk.hydrozoa.hydrowiki.Util;
import dk.hydrozoa.hydrowiki.database.DbArticles;
import dk.hydrozoa.hydrowiki.database.DbUsers;
import dk.hydrozoa.hydrowiki.handlers.IHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.UrlEncoded;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class NewArticleHandler extends IHandler {

    public NewArticleHandler(ServerContext ctx) {
        super(ctx);
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) {
        // not logged in, refuse service
        if (getLoggedIn(request) == null) {
            return false;
        }

        return switch (request.getMethod()) {
            case "POST" -> handlePost(request, response, callback);
            default -> handleGet(request, response, callback);
        };
    }

    // todo handle ?title=new_article_title to preset the name of the article
    private boolean handleGet(Request request, Response response, Callback callback) {
        Map model = Map.of();
        String content = Templater.renderTemplate("new_article.ftl", model);
        String fullPage = Templater.renderBaseTemplate(request, "New article", content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }

    private boolean handlePost(Request request, Response response, Callback callback) {
        DbUsers.RUser user = getLoggedIn(request);

        String articleTitle = null;
        String articleContent = null;
        try {
            Fields f = Request.getParameters(request);
            articleTitle = f.getValue("articleTitle").trim();
            articleContent = f.getValue("articleContent");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (Connection con = getContext().getDBConnectionPool().getConnection()) {
            int id = DbArticles.insertArticle(articleTitle, articleContent, con, getDatabaseLookupCounter());

            String diffToPrevious = Util.generateDiffs(articleTitle, articleContent, "");
            DbArticles.insertArticleEdit(user.id(), id, diffToPrevious, articleContent.length(), con, getDatabaseLookupCounter());

            if (id != -1) {
                Response.sendRedirect(request, response, callback, "/w/"+UrlEncoded.encodeString(articleTitle));
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return handleGet(request, response, callback);
    }
}
