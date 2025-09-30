package dk.hydrozoa.hydrowiki.handlers.pages;

import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.database.Counter;
import dk.hydrozoa.hydrowiki.database.DbArticles;
import dk.hydrozoa.hydrowiki.handlers.IHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.UrlEncoded;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

public class RandomArticleHandler extends IHandler {

    private Random random;

    public RandomArticleHandler(ServerContext ctx) {
        super(ctx);
        random = new Random();
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) {

        try (Connection con = getContext().getDBConnectionPool().getConnection()) {
            List<DbArticles.RArticle> list = DbArticles.getAllArticles(con, new Counter());
            int index = random.nextInt(list.size());
            DbArticles.RArticle selected = list.get(index);
            String urlEncodedTitle = UrlEncoded.encodeString(selected.title());
            Response.sendRedirect(request, response, callback, "/w/"+urlEncodedTitle);
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
