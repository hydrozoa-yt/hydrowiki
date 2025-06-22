package dk.hydrozoa.hydrowiki.handlers.util;

import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.util.function.Function;

/**
 * Wrapper around handler that allows rewriting of path before delegating.
 */
public class PathNameWrapper extends Handler.Wrapper {

    private final Function<String, String> nameFunction;

    public PathNameWrapper(Function<String, String> nameFunction, Handler handler) {
        super(handler);
        this.nameFunction = nameFunction;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        String originalPath = request.getHttpURI().getPath();

        String newPath = nameFunction.apply(originalPath);

        HttpURI newURI = HttpURI.build(request.getHttpURI()).path(newPath);

        Request wrappedRequest = new Request.Wrapper(request) {
            @Override
            public HttpURI getHttpURI() {
                return newURI;
            }
        };

        return super.handle(wrappedRequest, response, callback);
    }
}