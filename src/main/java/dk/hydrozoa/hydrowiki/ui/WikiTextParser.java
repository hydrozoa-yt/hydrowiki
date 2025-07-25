package dk.hydrozoa.hydrowiki.ui;

// todo add support for images [[media:test.png]]
// todo add support for external links [[link:example.com]]
// todo add support for [ref]example.com[/res] and [[resources]]
// todo add support for lists (ordered and unordered)
public class WikiTextParser {

    private static final String HEADING_1_ID = "=";

    public String parse(String input) {
        StringBuilder result = new StringBuilder();

        String[] lines = input.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].strip();

            if (line.endsWith(HEADING_1_ID) && line.startsWith(HEADING_1_ID)) {
                String stripped = line.substring(1, line.length()-1);
                result.append("<h2>");
                result.append(stripped);
                result.append("</h2>");
                continue;
            }

            String linkProcessed = processLinks(line);

            result.append("<p>");
            result.append(linkProcessed);
            result.append("</p>");
        }


        return result.toString();
    }

    /**
     * Parses one line of text and replaces [[links]] with <a>links</a>.
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
