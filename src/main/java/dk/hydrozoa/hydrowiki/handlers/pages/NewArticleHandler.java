package dk.hydrozoa.hydrowiki.handlers.pages;

import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.Templater;
import dk.hydrozoa.hydrowiki.handlers.IHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.util.Map;

public class NewArticleHandler extends IHandler {

    public NewArticleHandler(ServerContext ctx) {
        super(ctx);
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        Map model = Map.of();
        String content = Templater.renderTemplate("new_article.ftl", model);
        String fullPage = Templater.renderBaseTemplate("New article", content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }
}
