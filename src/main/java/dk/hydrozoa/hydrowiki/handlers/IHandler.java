package dk.hydrozoa.hydrowiki.handlers;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public abstract class IHandler extends Handler.Abstract {

    protected void sendHtml(int status, String content, Response response, Callback callback) {
        response.setStatus(status);
        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/html; charset=UTF-8");
        Content.Sink.write(response, true, content, callback);
    }
}
