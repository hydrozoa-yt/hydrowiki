package dk.hydrozoa.hydrowiki;

import dk.hydrozoa.hydrowiki.database.DbUsers;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.eclipse.jetty.server.Session;
import org.eclipse.jetty.server.Request;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class Templater {

    private static Configuration config;

    static {
        config = new Configuration(Configuration.VERSION_2_3_34);
        try {
            config.setDirectoryForTemplateLoading(new File("data/template/"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        config.setDefaultEncoding("UTF-8");
    }

    public static String renderBaseTemplate(Request request, String title, String content) {
        Session s = request.getSession(false);
        DbUsers.RUser user = null;

        if (s != null) {
            user = (DbUsers.RUser) s.getAttribute("user");
        }

        boolean loggedIn = false;
        if (user != null) {
            loggedIn = true;
        }

        try {
            Template temp = config.getTemplate("base.ftl");

            Map model = new HashMap();
            model.put("title", title);
            model.put("content", content);
            model.put("loggedIn", loggedIn);

            StringWriter w = new StringWriter();
            temp.process(model, w);
            return w.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    public static String renderTemplate(String templateName, Map model) {
        try {
            Template temp = config.getTemplate(templateName);

            StringWriter w = new StringWriter();
            temp.process(model, w);
            return w.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }
    }
}
