package org.rapaio.jupyter.kernel.core.display.html.tags;

import static org.rapaio.jupyter.kernel.core.display.html.Tags.b;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.br;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.each;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.iif;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.join;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.p;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.texts;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.TestUtils;
import org.rapaio.jupyter.kernel.core.display.html.JavadocTools;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;

import jdk.jshell.SourceCodeAnalysis;

public class TagsTest {

    @Test
    void tagsTest() {

        JavaEngine engine = JavaEngine.builder(TestUtils.getTestJShellConsole())
                .withTimeoutMillis(-1L)
                .build();

        int pos = 3;
        String source = "display(12);";
        while (pos + 1 < source.length() && Character.isJavaIdentifierPart(source.charAt(pos + 1))) {
            pos++;
        }

        int parenIdx = pos;
        while (parenIdx + 1 < source.length() && Character.isWhitespace(source.charAt(parenIdx + 1))) {
            parenIdx++;
        }
        if (parenIdx + 1 < source.length() && source.charAt(parenIdx + 1) == '(') {
            pos = parenIdx + 1;
        }

        List<SourceCodeAnalysis.Documentation> documentations = engine.getSourceAnalysis().documentation(source, pos + 1, true);

        String html = join(
                each(documentations, doc -> p(join(
                        b(texts(doc.signature())),
                        iif(doc.javadoc() != null,
                                br(),
                                texts(JavadocTools.javadocPreprocess(doc.javadoc()))
                        )
                )))
        ).render();
        System.out.println(html);
    }
}
