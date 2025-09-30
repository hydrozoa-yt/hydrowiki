package dk.hydrozoa.hydrowiki.handlers;

import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.database.Counter;
import dk.hydrozoa.hydrowiki.database.DbUsers;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Session;
import org.eclipse.jetty.util.Callback;

public abstract class IHandler extends Handler.Abstract {

    private ServerContext ctx;

    public IHandler(ServerContext ctx) {
        this.ctx = ctx;
    }

    protected void sendHtml(int status, String content, Response response, Callback callback) {
        response.setStatus(status);
        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/html; charset=UTF-8");
        Content.Sink.write(response, true, content, callback);
    }

    protected DbUsers.RUser getLoggedIn(Request request) {
        Session s = request.getSession(false);
        if (s == null) {
            return null;
        }

        DbUsers.RUser user = (DbUsers.RUser) s.getAttribute("user");
        return user;
    }

    protected ServerContext getContext() {
        return ctx;
    }

}
