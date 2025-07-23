package dk.hydrozoa.hydrowiki.handlers.pages;

import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.Templater;
import dk.hydrozoa.hydrowiki.database.DbArticles;
import dk.hydrozoa.hydrowiki.handlers.IHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AllArticlesHandler extends IHandler {

    public AllArticlesHandler(ServerContext ctx) {
        super(ctx);
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        List<DbArticles.RArticle> results = List.of();
        try (Connection con = getContext().getDBConnectionPool().getConnection()) {
            results = DbArticles.getAllArticles(con, getDatabaseLookupCounter());
        }

        List<Map> articles = new ArrayList<>();
        results.forEach(row -> {
            articles.add(Map.of(
                    "title", row.title()
                    )
            );
        });

        String content = Templater.renderTemplate("all_articles.ftl", Map.of("articles", articles));
        String fullPage = Templater.renderBaseTemplate(request, "All articles", content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }
}
