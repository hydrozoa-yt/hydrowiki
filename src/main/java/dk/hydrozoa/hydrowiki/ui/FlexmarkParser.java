package dk.hydrozoa.hydrowiki.ui;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

// todo add visitor to headings and change their levels
// todo configure wikilink plugin
// todo configure image plugin
public class FlexmarkParser {

    public String parse(String input) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(input);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

}
