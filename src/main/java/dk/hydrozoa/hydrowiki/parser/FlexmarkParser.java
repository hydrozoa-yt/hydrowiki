package dk.hydrozoa.hydrowiki.parser;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ext.wikilink.WikiImage;
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.AttributeProviderFactory;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.html.MutableAttributes;
import com.vladsch.flexmark.util.misc.Extension;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Parser that uses flexmark for building a model of the input, and html rendering.
 * This is currently the main parser used.
 */
public class FlexmarkParser {

    static class ImageAttributeProvider implements AttributeProvider {
        static AttributeProviderFactory Factory() {
            return new IndependentAttributeProviderFactory() {
                @Override
                public @NotNull AttributeProvider apply(@NotNull LinkResolverContext linkResolverContext) {
                    return new ImageAttributeProvider();
                }
            };
        }

        @Override
        public void setAttributes(@NotNull Node node, @NotNull AttributablePart attributablePart, @NotNull MutableAttributes mutableAttributes) {
            if (node instanceof Image) {
                mutableAttributes.addValue("class", "img-fluid");
            }
            if (node instanceof WikiImage) {
                mutableAttributes.addValue("class", "img-fluid");
            }
        }
    }

    NodeVisitor headerVisitor = new NodeVisitor(
            new VisitHandler<>(Heading.class, this::visitHeadings)
    );

    private DataHolder dataHolder;

    private String s3url;

    public FlexmarkParser(String s3url) {
        this.s3url = s3url;
        MutableDataSet dataSet = new MutableDataSet();
        ArrayList<Extension> extensions = new ArrayList<>();

        // Configure wikilinks
        extensions.add(WikiLinkExtension.create());
        dataSet.set(WikiLinkExtension.DISABLE_RENDERING, false);
        dataSet.set(WikiLinkExtension.LINK_FIRST_SYNTAX, true);
        dataSet.set(WikiLinkExtension.LINK_PREFIX, "/w/");
        dataSet.set(WikiLinkExtension.IMAGE_LINKS, true);
        dataSet.set(WikiLinkExtension.IMAGE_PREFIX, s3url+"/");

        dataSet.set(Parser.EXTENSIONS, extensions);
        dataHolder = dataSet;
    }

    public void visitHeadings(Heading h) {
        h.setLevel(h.getLevel()+1);

        // Descending into children
        headerVisitor.visitChildren(h);
    }

    public String parse(String input) {
        Parser parser = Parser.builder(dataHolder).build();
        Node document = parser.parse(input);

        headerVisitor.visit(document);

        HtmlRenderer renderer = HtmlRenderer
                .builder(dataHolder)
                .attributeProviderFactory(ImageAttributeProvider.Factory())
                .build();
        return renderer.render(document);
    }
}
