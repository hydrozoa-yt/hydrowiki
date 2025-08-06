package dk.hydrozoa.hydrowiki.handlers.pages;

import dk.hydrozoa.hydrowiki.S3Interactor;
import dk.hydrozoa.hydrowiki.ServerContext;
import dk.hydrozoa.hydrowiki.Templater;
import dk.hydrozoa.hydrowiki.database.Counter;
import dk.hydrozoa.hydrowiki.database.DbMedia;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;

/**
 * Displays list of all media currently in the database as well as has an option to upload new media.
 * Should also be able to serve media at /media/?name=example.jpeg, although in production those would be served by
 * nginx or similar without invoking the app.
 */
public class MediaHandler extends IHandler {

    final Logger logger = LoggerFactory.getLogger(MediaHandler.class);

    private String S3_URL;

    /**
     * Max filesize for uploaded media in megabytes.
     */
    private final int MAX_UPLOAD_SIZE = 4;

    public MediaHandler(ServerContext ctx) {
        super(ctx);
        S3_URL = ctx.getProperties().getProperty("s3.public_access");
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
                return handleGet(request, response, callback);
        }
    }

    private boolean handleGet(Request request, Response response, Callback callback) {
        InfoMessage.Message message = (InfoMessage.Message) request.getSession(false).getAttribute("infoMessage");
        request.getSession(false).removeAttribute("infoMessage");

        List<DbMedia.RMedia> allMedia = List.of();
        try (Connection con = getContext().getDBConnectionPool().getConnection()) {
            allMedia = DbMedia.getAllMedia(con, new Counter());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        List<Map> mediaModel = new ArrayList<>();
        allMedia.forEach(i -> {
            Map model = Map.of("filename", i.filename(),
                    "url", S3_URL+"/"+i.filename()
            );
            mediaModel.add(model);
        });

        Map model = new HashMap();
        model.put("medias", mediaModel);
        if (message != null) {
            model.put("infoMessage", InfoMessage.toModel(message));
        }

        String content = Templater.renderTemplate("media_list.ftl", model);
        String fullPage = Templater.renderBaseTemplate(request,"HydroWiki", content);
        sendHtml(200, fullPage, response, callback);
        return true;
    }

    private boolean handlePost(Request request, Response response, Callback callback) {
        DbUsers.RUser user = getLoggedIn(request);
        if (user == null) { // refuse service if not logged in
            return false;
        }

        String contentType = request.getHeaders().get("Content-Type");
        MultiPartConfig config = new MultiPartConfig.Builder()
                .location(Path.of("/tmp/"))
                .maxPartSize(MAX_UPLOAD_SIZE * 1024 * 1024) // max 1 MB
                .build();

        MultiPartFormData.Parts parts = null;
        try {
            parts = MultiPartFormData.getParts(request, request, contentType, config);
        } catch (CompletionException e) {
            if (e.getCause() instanceof IllegalStateException) {
                InfoMessage.Message errorMessage = new InfoMessage.Message(InfoMessage.TYPE.ERROR, "Media filesize too large");
                request.getSession(true).setAttribute("infoMessage", errorMessage);

                Response.sendRedirect(request, response, callback, "/media/", true);
                return true;
            } else {
                InfoMessage.Message errorMessage = new InfoMessage.Message(InfoMessage.TYPE.ERROR, "Unexpected error");
                request.getSession(true).setAttribute("infoMessage", errorMessage);

                Response.sendRedirect(request, response, callback, "/media/", true);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        MultiPart.Part filePart = parts.iterator().next();
        String filename = filePart.getFileName();
        if (getContext().getS3Interactor().fileExists(filename)) {
            InfoMessage.Message message = new InfoMessage.Message(InfoMessage.TYPE.ERROR, "File with identical name already exists");
            request.getSession(true).setAttribute("infoMessage", message);

            Response.sendRedirect(request, response, callback, "/media/", true);
            return true;
        }
        byte[] fileBytes;
        try {
            fileBytes = Content.Source.asInputStream(filePart.getContentSource()).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Upload file to s3 bucket
        getContext().getS3Interactor().uploadFile(filename, fileBytes);
        // Add entry to database
        int mediaId = -1;
        try (Connection con = getContext().getDBConnectionPool().getConnection()) {
            mediaId = DbMedia.insertMedia(filename, user.id(), con, new Counter());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        InfoMessage.Message message = new InfoMessage.Message(InfoMessage.TYPE.SUCCESS, "Successfully uploaded media");
        request.getSession(true).setAttribute("infoMessage", message);

        Response.sendRedirect(request, response, callback, "/media/", true);
        return true;
    }
}
