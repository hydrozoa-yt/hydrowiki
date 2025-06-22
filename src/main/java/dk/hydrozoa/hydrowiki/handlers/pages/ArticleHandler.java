package dk.hydrozoa.hydrowiki.handlers.pages;

import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.Templater;
import dk.hydrozoa.hydrowiki.Util;
import dk.hydrozoa.hydrowiki.database.DbArticles;
import dk.hydrozoa.hydrowiki.handlers.IHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class ArticleHandler extends IHandler {

    public ArticleHandler(ServerContext ctx) {
        super(ctx);
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
                return handleGet(pathTokens, request, response, callback);
        }
    }

    private boolean handleGet(String[] pathTokens, Request request, Response response, Callback callback) {
        String articleName = pathTokens[1];

        DbArticles.RArticle article = null;
        try (Connection con = getContext().getDBConnectionPool().getConnection()) {
            article = DbArticles.get(articleName, con, getDatabaseLookupCounter());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (article == null) {
            return false;
        }

        Map model = Map.of(
                "articleName", article.title(),
                "articleContent", article.content()
        );

        String content = Templater.renderTemplate("article.ftl", model);
        String fullPage = Templater.renderBaseTemplate(articleName, content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }

    private boolean handlePost(String[] pathTokens, Request request, Response response, Callback callback) {
        // todo handle
        return handleGet(pathTokens, request, response, callback);
    }

}
