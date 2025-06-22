package dk.hydrozoa.hydrowiki.handlers.util;

import org.eclipse.jetty.server.Handler;

/**
 * Wrapper around handler that removes part of the path before delegating.
 */
public class StripContextPathWrapper extends PathNameWrapper {
    public StripContextPathWrapper(String contextPath, Handler handler) {
        super((path) -> {
            if (path.startsWith(contextPath))
                return path.substring(contextPath.length());
            return path;
        }, handler);
    }
}