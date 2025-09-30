package dk.hydrozoa.hydrowiki.handlers.pages;

import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.Templater;
import dk.hydrozoa.hydrowiki.Util;
import dk.hydrozoa.hydrowiki.database.Counter;
import dk.hydrozoa.hydrowiki.database.DbArticles;
import dk.hydrozoa.hydrowiki.database.DbArticles.RArticle;
import dk.hydrozoa.hydrowiki.handlers.IHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchHandler extends IHandler {

    public SearchHandler(ServerContext ctx) {
        super(ctx);
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        Fields fields = Request.extractQueryParameters(request);
        String terms = fields.getValue("terms");
        if (terms == null) {
            Response.sendRedirect(request, response, callback, "/");
            return true;
        }

        RArticle aliasArticle = null;
        List<RArticle> results = List.of();
        try (Connection con = getContext().getDBConnectionPool().getConnection()) {
            aliasArticle = DbArticles.searchArticleAlias(terms, con, new Counter());
            if (aliasArticle == null) { // did not find alias, do full text search
                results = DbArticles.searchArticles(terms, con, new Counter());
            }
        }

        if (aliasArticle != null) { // redirect if found alias
            request.getSession(true).setAttribute(ArticleHandler.SESSION_ATTRIB_REDIRECT, terms);
            Response.sendRedirect(request, response, callback, "/w/"+Util.articleTitleToUrl(aliasArticle.title()));
            return true;
        }

        List<Map> resultsModel = new ArrayList<>();
        for (DbArticles.RArticle a : results) {
            Map entry = Map.of("title", a.title(),
                    "content", a.content());
            resultsModel.add(entry);
        }

        Map model = Map.of("terms", terms,
                "resultSize", results.size(),
                "results", resultsModel
        );

        String content = Templater.renderTemplate("search.ftl", model);
        String fullPage = Templater.renderBaseTemplate(request, "Search results", content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }
}
