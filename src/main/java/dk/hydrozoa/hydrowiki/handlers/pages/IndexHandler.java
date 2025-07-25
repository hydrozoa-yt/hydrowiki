package dk.hydrozoa.hydrowiki.handlers.pages;

import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.Templater;
import dk.hydrozoa.hydrowiki.handlers.IHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.util.Map;

public class IndexHandler extends IHandler {

    public IndexHandler(ServerContext ctx) {
        super(ctx);
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) {
        String content = Templater.renderTemplate("index.ftl", Map.of());
        String fullPage = Templater.renderBaseTemplate(request,"HydroWiki", content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }

}
