package dk.hydrozoa.hydrowiki.handlers.pages;

import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.Templater;
import dk.hydrozoa.hydrowiki.database.DbUsers;
import dk.hydrozoa.hydrowiki.handlers.IHandler;
import dk.hydrozoa.hydrowiki.model.InfoMessage;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.util.Map;

/**
 * Displays list of all media currently in the database as well as has an option to upload new media.
 * Should also be able to serve media at /media?name=example.jpeg, although in production those would be served by
 * nginx or similar without invoking the app.
 */
public class MediaHandler extends IHandler {

    public MediaHandler(ServerContext ctx) {
        super(ctx);
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        DbUsers.RUser user = getLoggedIn(request);
        if (user != null) {
            Response.sendRedirect(request, response, callback, "/");
            return true;
        }

        switch (request.getMethod()) {
            case "POST":
                return handlePost(request, response, callback);
            case "GET":
            default:
                return handleGet(null, request, response, callback);
        }
    }

    private boolean handleGet(InfoMessage.Message message, Request request, Response response, Callback callback) {
        String content = Templater.renderTemplate("media_list.ftl", Map.of());
        String fullPage = Templater.renderBaseTemplate(request,"HydroWiki", content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }

    private boolean handlePost(Request request, Response response, Callback callback) {
        return false;
    }
}
