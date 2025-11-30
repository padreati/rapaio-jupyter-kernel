package org.rapaio.jupyter.kernel.display.html.tags;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.rapaio.jupyter.kernel.display.html.Tags.b;
import static org.rapaio.jupyter.kernel.display.html.Tags.br;
import static org.rapaio.jupyter.kernel.display.html.Tags.each;
import static org.rapaio.jupyter.kernel.display.html.Tags.iif;
import static org.rapaio.jupyter.kernel.display.html.Tags.join;
import static org.rapaio.jupyter.kernel.display.html.Tags.p;
import static org.rapaio.jupyter.kernel.display.html.Tags.texts;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.display.html.JavadocTools;
import org.rapaio.jupyter.kernel.display.html.Tag;

import jdk.jshell.SourceCodeAnalysis;

public class TagsTest {

    @Test
    void tagsTest() {
        List<SourceCodeAnalysis.Documentation> documentations = List.of(
                doc("signature1", "javadoc1"),
                doc("signature2", "javadoc2")
        );

        String html = join(
                each(documentations, doc -> p(join(
                        b(texts(doc.signature())),
                        iif(doc.javadoc() != null,
                                () -> new Tag[] {br(), texts(JavadocTools.javadocPreprocess(doc.javadoc()))}
                        )
                )))
        ).render();
        assertEquals("<p><b>signature1</b><br/>javadoc1</p><p><b>signature2</b><br/>javadoc2</p>", html);
    }

    private static SourceCodeAnalysis.Documentation doc(String signature, String javadoc) {
        return new SourceCodeAnalysis.Documentation() {

            @Override
            public String signature() {
                return signature;
            }

            @Override
            public String javadoc() {
                return javadoc;
            }
        };
    }
}
