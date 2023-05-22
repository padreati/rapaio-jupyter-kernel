package org.rapaio.jupyter.kernel.core.magic;

import static org.rapaio.jupyter.kernel.core.display.html.Tags.b;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.br;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.each;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.join;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.space;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.texts;

import java.util.List;

import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;

public abstract class MagicHandler {

    /**
     * @return name of the magic handler useful in help messages
     */
    public abstract String name();

    /**
     * General help documentation lines.
     *
     * @return general documentation lines
     */
    public abstract List<String> helpMessage();

    public abstract List<OneLineMagicHandler> oneLineMagicHandlers();

    public abstract boolean canHandleSnippet(MagicSnippet magicSnippet);

    public final Object eval(RapaioKernel kernel, MagicSnippet magicSnippet) throws MagicParseException, MagicEvalException {
        if (!canHandleSnippet(magicSnippet)) {
            throw new RuntimeException("Try to execute a magic snippet to improper handler.");
        }
        for (OneLineMagicHandler handler : oneLineMagicHandlers()) {
            if (handler.canHandlePredicate().test(magicSnippet)) {
                return handler.evalFunction().apply(kernel, magicSnippet);
            }
        }
        throw new MagicEvalException(magicSnippet, "Command not executed either because there is no handler or due to a syntax error.");
    }

    public final DisplayData inspect(RapaioKernel kernel, MagicSnippet magicSnippet) throws MagicEvalException, MagicParseException {
        // if we can be more specific, than do it
        if (oneLineMagicHandlers().size() > 1) {
            for (var handler : oneLineMagicHandlers()) {
                if (handler.canHandlePredicate().test(magicSnippet)) {
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

    protected boolean canHandleOneLinePrefix(MagicSnippet magicSnippet, String prefix) {
        return magicSnippet.oneLine() && magicSnippet.lines().size() == 1 && magicSnippet.lines().get(0).code().startsWith(prefix);
    }
}
