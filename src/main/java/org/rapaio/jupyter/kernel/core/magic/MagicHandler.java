package org.rapaio.jupyter.kernel.core.magic;

import static org.rapaio.jupyter.kernel.core.display.html.Tags.b;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.br;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.each;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.join;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.space;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.texts;

import java.util.List;

import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;

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

    boolean canHandleSnippet(MagicSnippet magicSnippet);

    default Object eval(RapaioKernel kernel, MagicSnippet snippet) throws MagicParseException,
            MagicEvalException {
        for (OneLineMagicHandler handler : oneLineMagicHandlers()) {
            if (handler.canHandlePredicate().test(snippet)) {
                return handler.evalFunction().apply(kernel, snippet);
            }
        }
        throw new MagicEvalException(snippet, "Couldn't nou find handler for command.");
    }

    default DisplayData inspect(RapaioKernel kernel, MagicSnippet magicSnippet) throws MagicEvalException, MagicParseException {
        // if we can be more specific, than do it
        if (oneLineMagicHandlers().size() > 1) {
            for(var handler : oneLineMagicHandlers()) {
                if(handler.canHandlePredicate().test(magicSnippet)) {
                    return handler.inspectFunction().apply(kernel, magicSnippet);
                }
            }
        }
        // otherwise show generic help
        String inspectHtml = join(
                b(texts(name())),
                br(),
                each(helpMessage(), line -> join(texts(line), br())),
                texts("Syntax: "), br(),
                each(oneLineMagicHandlers(), handler -> join(
                                b(texts(handler.syntaxHelp())),
                                br(),
                                each(handler.documentation(), line -> join(space(4), texts(line), br()))
                        )
                )
        ).render();

        StringBuilder sb = new StringBuilder();
        sb.append(ANSI.start().bold().text(name()).render()).append("\n\n");
        sb.append(String.join("\n", helpMessage())).append("\n\n");
        sb.append("Syntax:\n");
        for (var handler : oneLineMagicHandlers()) {
            sb.append(ANSI.start().bold().text(handler.syntaxHelp()).render()).append("\n");
            for (var line : handler.documentation()) {
                sb.append("    ").append(line).append("\n");
            }
        }

        DisplayData dd = DisplayData.withHtml(inspectHtml);
        dd.putText(sb.toString());
        return dd;
    }

    default CompleteMatches complete(RapaioKernel kernel, MagicSnippet snippet) {
        return null;
    }
}
