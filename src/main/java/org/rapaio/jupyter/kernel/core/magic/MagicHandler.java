package org.rapaio.jupyter.kernel.core.magic;

import static org.rapaio.jupyter.kernel.core.display.html.Tags.b;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.br;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.each;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.join;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.p;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.space;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.texts;

import java.util.List;

import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.Suggestions;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;

public interface MagicHandler {

    String name();

    List<String> syntax();

    List<String> helpMessage();

    boolean canHandleSnippet(MagicSnippet snippet);

    Object eval(MagicEngine magicEvaluator, JavaEngine engine, Channels channels, MagicSnippet snippet) throws MagicParseException,
            MagicEvalException;

    default DisplayData inspect(Channels channels, MagicSnippet snippet) {
        return DisplayData.withHtml(
                join(
                        p(texts("Syntax: "), br(),
                                each(syntax(), syntaxLine -> b(space(4), texts(syntaxLine)))
                        ),
                        p(each(helpMessage(), line -> p(texts(line))))
                ).render()
        );
    }

    Suggestions complete(Channels channels, MagicSnippet snippet);
}
