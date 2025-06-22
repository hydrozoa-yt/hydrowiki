package dk.hydrozoa.hydrowiki.handlers.pages;

import dk.hydrozoa.hydrowiki.Templater;
import dk.hydrozoa.hydrowiki.handlers.IHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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
        Map model = new HashMap();
        String content = Templater.renderTemplate("index.ftl", model);
        String fullPage = Templater.renderBaseTemplate("Index page title", content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }

}
