package dk.hydrozoa.hydrowiki.handlers.pages;

import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.Templater;
import dk.hydrozoa.hydrowiki.WikiServer;
import dk.hydrozoa.hydrowiki.database.DbUsers;
import dk.hydrozoa.hydrowiki.handlers.IHandler;
import dk.hydrozoa.hydrowiki.model.InfoMessage;
import org.eclipse.jetty.http.MultiPart;
import org.eclipse.jetty.http.MultiPartConfig;
import org.eclipse.jetty.http.MultiPartFormData;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Displays list of all media currently in the database as well as has an option to upload new media.
 * Should also be able to serve media at /media?name=example.jpeg, although in production those would be served by
 * nginx or similar without invoking the app.
 */
public class MediaHandler extends IHandler {

    final Logger logger = LoggerFactory.getLogger(MediaHandler.class);

    public MediaHandler(ServerContext ctx) {
        super(ctx);
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        DbUsers.RUser user = getLoggedIn(request);
        if (user == null) {
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
        String infoMessage = message == null ? "" : message.message();

        String content = Templater.renderTemplate("media_list.ftl", Map.of("infoMessage", infoMessage));
        String fullPage = Templater.renderBaseTemplate(request,"HydroWiki", content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }

    private boolean handlePost(Request request, Response response, Callback callback) {
        if (getLoggedIn(request) == null) { // refuse service if not logged in
            return false;
        }

        String contentType = request.getHeaders().get("Content-Type");
        MultiPartConfig config = new MultiPartConfig.Builder()
                .location(Path.of("/tmp/"))
                .maxPartSize(1024 * 1024) // max 1 MB
                .build();

        MultiPartFormData.Parts parts = MultiPartFormData.getParts(request, request, contentType, config);
        MultiPart.Part filePart = parts.iterator().next();
        if (getContext().getS3Interactor().fileExists(filePart.getFileName())) {
            return handleGet(new InfoMessage.Message(InfoMessage.TYPE.ERROR, "File with identical name already exists"), request, response, callback);
        }
        byte[] fileBytes;
        try {
            fileBytes = Content.Source.asInputStream(filePart.getContentSource()).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        getContext().getS3Interactor().uploadFile(filePart.getFileName(), fileBytes);

        return handleGet(new InfoMessage.Message(InfoMessage.TYPE.SUCCESS, "Uploaded image"), request, response, callback);
    }
}
