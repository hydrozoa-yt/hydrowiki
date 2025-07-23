package dk.hydrozoa.hydrowiki.handlers.pages;

import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.Templater;
import dk.hydrozoa.hydrowiki.database.Counter;
import dk.hydrozoa.hydrowiki.database.DbUsers;
import dk.hydrozoa.hydrowiki.handlers.IHandler;
import org.eclipse.jetty.server.FormFields;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Session;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;

import java.sql.Connection;
import java.sql.SQLException;
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
        String fullPage = Templater.renderBaseTemplate(request, "Login", content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }

    private boolean handlePost(Request request, Response response, Callback callback) {
        Fields fields = FormFields.getFields(request);

        if (fields != null) {
            String username = fields.getValue("usernameInput");
            String password = fields.getValue("passwordInput");

            DbUsers.RUser user = null;
            try (Connection con = getContext().getDBConnectionPool().getConnection()) {
                user = DbUsers.getUser(username, con, new Counter());
                System.out.println("username: " + username + " \t password: " + password);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            if (user == null) {
                return handleGet("Login unsuccessful", request, response, callback);
            }

            if (!user.password().equals(password)) {
                return handleGet("Login unsuccessful", request, response, callback);
            }

            // attach copy of user object to session
            Session s = request.getSession(true);
            s.setAttribute("user", user);

            Response.sendRedirect(request, response, callback, "/");
            return true;
        }

        // if no fields refuse service
        return false;
    }
}
