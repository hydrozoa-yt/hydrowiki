package dk.hydrozoa.hydrowiki.ui;

import java.util.Arrays;
import java.util.Iterator;

// todo add support for images [[media:test.png]]
// todo add support for external links [[link:example.com]]
// todo add support for [ref]example.com[/ref] and [[resources]]
// todo add support for lists (ordered and unordered)
public class WikiTextParser {

    private static final String HEADING_1_ID = "=";
    private static final String HEADING_2_ID = "==";

    public String parse(String input) {
        StringBuilder result = new StringBuilder();

        // todo make queue to allow peek and poll
        Iterator<String> lines = Arrays.stream(input.split("\n")).iterator();
        String line = null;
        while (lines.hasNext()) {
            if (line == null) {
                line = lines.next();
            }

            // todo consolidate these two blocks into a method
            if (line.endsWith(HEADING_1_ID) && line.startsWith(HEADING_1_ID)) {
                String stripped = line.substring(HEADING_1_ID.length(), line.length()-HEADING_1_ID.length());
                result.append("<h2>");
                result.append(stripped);
                result.append("</h2>");
                line = lines.next();
                continue;
            }

            if (line.endsWith(HEADING_2_ID) && line.startsWith(HEADING_2_ID)) {
                String stripped = line.substring(HEADING_2_ID.length(), line.length()-HEADING_2_ID.length());
                result.append("<h3>");
                result.append(stripped);
                result.append("</h3>");
                line = lines.next();
                continue;
            }

            if (line.strip().startsWith("[[references]]")) {
                // todo insert reference table
            }

            String linkProcessed = processLinks(line);
            String textmodifyingTagsProcessed = processTextModifyingTags(linkProcessed);

            result.append("<p>");
            result.append(textmodifyingTagsProcessed);
            result.append("</p>");

            line = lines.next();
        }

        return result.toString();
    }

    private String processReferences(String input) {
        if (!input.contains("[ref]")) {
            return input;
        }

        // todo

        return input;
    }

    /**
     * Parses one line of text and replaces [b]example[/b] with html.
     */
    private String processTextModifyingTags(String input) {
        return input.replace("[b]", "<b>")
                .replace("[/b]", "</b>")
                .replace("[i]", "<i>")
                .replace("[/i]", "</i>")
                ;
    }

    /**
     * Parses one line of text and replaces [[links]] with html.
     */
    private String processLinks(String input) {
        if (!input.contains("[[")) {
            return input;
        }

        StringBuilder sb = new StringBuilder();

        int linkSearchCurrentIndex = 0;
        int linkStart = 0;
        while (true) {
            linkStart = input.indexOf("[[", linkSearchCurrentIndex);
            if (linkStart == -1) {
                // output rest of line
                sb.append(input.substring(linkSearchCurrentIndex));
                return sb.toString();
            }

            int linkEnd = input.indexOf("]]", linkStart);
            if (linkEnd == -1) {
                // output rest of line
                sb.append(input.substring(linkSearchCurrentIndex));
                return sb.toString();
            }

            // output text until link
            sb.append(input.substring(linkSearchCurrentIndex, linkStart));

            // retrieve text inside square brackets
            String linkText = input.substring(linkStart+2, linkEnd);

            // start next search
            linkSearchCurrentIndex = linkEnd+2;

            // output link
            sb.append("<a href=\"/w/"+linkText+"\">");
            sb.append(linkText);
            sb.append("</a>");
        }
    }
}
