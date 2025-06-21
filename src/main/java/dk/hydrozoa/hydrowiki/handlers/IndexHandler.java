package dk.hydrozoa.hydrowiki.handlers;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class IndexHandler extends IHandler {

    String indexText;

    public IndexHandler(String path) {
        try {
            this.indexText = Files.readString(Path.of("data", "index.html"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        sendHtml(200, indexText, response, callback);
        return true;
    }

}
