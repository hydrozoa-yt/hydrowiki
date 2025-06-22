package dk.hydrozoa.hydrowiki;

import dk.hydrozoa.hydrowiki.handlers.pages.ArticleHandler;
import dk.hydrozoa.hydrowiki.handlers.pages.IndexHandler;
import dk.hydrozoa.hydrowiki.handlers.pages.NewArticleHandler;
import dk.hydrozoa.hydrowiki.handlers.util.StripContextPathWrapper;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.PathMappingsHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.resource.Resources;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class WikiServer implements Runnable, ServerContext {

    private Properties config;

    public void run() {
        // Load properties
        config = new Properties();
        try {
            config.load(Files.newInputStream(Path.of("data", "config.properties")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create and configure a ThreadPool.
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName("wikiserver");

        // Create a Server instance.
        Server server = new Server(threadPool);

        // Create a ServerConnector to accept connections from clients.
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(Integer.parseInt(config.getProperty("app.port")));

        // Add the Connector to the Server
        server.addConnector(connector);

        // Make a handler that delegates to handlers based on path
        PathMappingsHandler mapHandler = new PathMappingsHandler();

        ResourceFactory resourceFactory = ResourceFactory.of(server);

        // Create and configure a ResourceHandler.
        Resource publicResource = resourceFactory.newResource(Path.of("data/public/"));
        if (!Resources.isReadableDirectory(publicResource)) {
            throw new RuntimeException("Resource is not a readable directory");
        }
        ResourceHandler publicFilesHandler = new ResourceHandler();
        publicFilesHandler.setBaseResource(publicResource);
        publicFilesHandler.setUseFileMapping(true);
        publicFilesHandler.setDirAllowed(false);

        MimeTypes.Mutable types = new MimeTypes.Mutable();
        types.addMimeMapping("js", "text/javascript; charset=utf-8");
        types.addMimeMapping("css", "text/css; charset=utf-8");
        types.addMimeMapping("svg", "image/svg+xml");
        publicFilesHandler.setMimeTypes(types);

        StripContextPathWrapper stripResHandler = new StripContextPathWrapper("/files", publicFilesHandler);

        mapHandler.addMapping(PathSpec.from("/files/*"), stripResHandler);
        mapHandler.addMapping(PathSpec.from("/w/*"), new ArticleHandler(this));
        mapHandler.addMapping(PathSpec.from("/new/"), new NewArticleHandler(this));
        mapHandler.addMapping(PathSpec.from("/"), new IndexHandler(this));

        // Set a simple Handler to handle requests/responses.
        server.setHandler(mapHandler);

        // logging per request
        server.setRequestLog(new CustomRequestLog(new Slf4jRequestLogWriter(), CustomRequestLog.NCSA_FORMAT));

        // Start the Server to start accepting connections from clients.
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Properties getProperties() {
        return config;
    }
}
