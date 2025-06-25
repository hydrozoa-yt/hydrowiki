package dk.hydrozoa.hydrowiki.ui;

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

            result.append("<p>");
            result.append(line);
            result.append("</p>");
        }


        return result.toString();
    }
}
