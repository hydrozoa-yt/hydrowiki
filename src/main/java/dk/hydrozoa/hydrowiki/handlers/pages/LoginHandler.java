package dk.hydrozoa.hydrowiki.handlers.pages;

import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.Templater;
import dk.hydrozoa.hydrowiki.Util;
import dk.hydrozoa.hydrowiki.database.DbArticles;
import dk.hydrozoa.hydrowiki.handlers.IHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;

import java.sql.Connection;
import java.util.Map;

public class LoginHandler  extends IHandler {

    public LoginHandler(ServerContext ctx) {
        super(ctx);
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        /*if (pathTokens.length < 2) {
            Response.sendRedirect(request, response, callback, "/");
            return true;
        }*/

        switch (request.getMethod()) {
            case "POST":
                return handlePost(request, response, callback);
            case "GET":
            default:
                return handleGet("", request, response, callback);
        }
    }

    private boolean handleGet(String message, Request request, Response response, Callback callback) {
        String content = Templater.renderTemplate("login.ftl", Map.of());
        String fullPage = Templater.renderBaseTemplate("Login", content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }

    private boolean handlePost(Request request, Response response, Callback callback) {
        try {
            Fields fields = Request.getParameters(request);
            if (fields != null) {
                String email = fields.getValue("emailInput");
                String password = fields.getValue("passwordInput");

                System.out.println("email: "+email+" \t password: "+password);
                // todo handle
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
