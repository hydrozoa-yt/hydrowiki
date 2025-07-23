package dk.hydrozoa.hydrowiki.handlers.pages;

import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.Templater;
import dk.hydrozoa.hydrowiki.database.DbArticles;
import dk.hydrozoa.hydrowiki.handlers.IHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecentChangesHandler extends IHandler {

    public RecentChangesHandler(ServerContext ctx) {
        super(ctx);
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        List<DbArticles.RArticleEditWithTitle> results = List.of();
        try (Connection con = getContext().getDBConnectionPool().getConnection()) {
            results = DbArticles.getRecentArticleEdits(con, getDatabaseLookupCounter());
        }

        List<Map> history = new ArrayList<>();
        results.forEach(row -> {
            history.add(Map.of(
                    "title", row.title(),
                    "version", row.edit().version(),
                    "timestamp", row.edit().created().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm"))
            ));
        });

        String content = Templater.renderTemplate("recent_changes.ftl", Map.of("history", history));
        String fullPage = Templater.renderBaseTemplate(request,"Recent changes", content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }
}
