package dk.hydrozoa.hydrowiki;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

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

    public static String renderBaseTemplate(String title, String content) {
        try {
            Template temp = config.getTemplate("base.ftl");

            Map model = new HashMap();
            model.put("title", title);
            model.put("content", content);

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
