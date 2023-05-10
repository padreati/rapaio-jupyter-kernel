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
import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;

public interface MagicHandler {

    /**
     * @return name of the magic handler useful in help messages
     */
    String name();

    /**
     * General help documentation lines.
     *
     * @return general documentation lines
     */
    List<String> helpMessage();

    List<OneLineMagicHandler> oneLineMagicHandlers();

    boolean canHandleSnippet(MagicSnippet snippet);

    default Object eval(MagicEngine magicEngine, JavaEngine engine, Channels channels, MagicSnippet snippet) throws MagicParseException,
            MagicEvalException {
        for(OneLineMagicHandler handler : oneLineMagicHandlers()) {
            if(handler.canHandlePredicate().test(snippet)) {
                return handler.evalFunction().eval(magicEngine, engine, channels, snippet);
            }
        }
        throw new MagicEvalException(snippet, "Counld nou find handler for command.");
    }

    default DisplayData inspect(Channels channels, MagicSnippet snippet) {
        return DisplayData.withHtml(
                join(
                        p(each(helpMessage(), line -> p(texts(line)))),
                        p(texts("Syntax: "), br(),
                                each(oneLineMagicHandlers(), handler -> join(texts(handler.syntaxHelp()), br(),
                                        each(handler.documentation(), line -> b(space(4), texts(line)))))
                        )
                ).render()
        );
    }

    CompleteMatches complete(Channels channels, MagicSnippet snippet);
}
