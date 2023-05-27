package org.rapaio.jupyter.kernel.core.magic;

import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;

import java.util.List;

import static org.rapaio.jupyter.kernel.core.display.html.Tags.*;

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

    public abstract List<SnippetMagicHandler> snippetMagicHandlers();

    public abstract boolean canHandleSnippet(MagicSnippet magicSnippet);

    public final Object eval(RapaioKernel kernel, MagicSnippet magicSnippet) throws MagicParseException, MagicEvalException {
        if (!canHandleSnippet(magicSnippet)) {
            throw new RuntimeException("Try to execute a magic snippet to improper handler.");
        }
        for (SnippetMagicHandler handler : snippetMagicHandlers()) {
            if (handler.canHandlePredicate().test(magicSnippet)) {
                return handler.evalFunction().apply(kernel, magicSnippet);
            }
        }
        throw new MagicEvalException(magicSnippet, "Command not executed either because there is no handler or due to a syntax error.");
    }

    public final DisplayData inspect(RapaioKernel kernel, MagicSnippet magicSnippet) throws MagicEvalException, MagicParseException {
        // if we can be more specific, than do it
        if (snippetMagicHandlers().size() > 1) {
            for (var handler : snippetMagicHandlers()) {
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
                each(snippetMagicHandlers(), handler -> join(
                                each(handler.syntaxHelp(), line -> join(b(texts(line)), br())),
                                each(handler.documentation(), line -> join(space(4), texts(line), br()))
                        )
                )
        ).render();

        StringBuilder sb = new StringBuilder();
        sb.append(ANSI.start().bold().text(name()).render()).append("\n\n");
        sb.append(String.join("\n", helpMessage())).append("\n\n");
        sb.append("Syntax:\n");
        for (var handler : snippetMagicHandlers()) {
            sb.append(ANSI.start().bold().text(String.join("\n", handler.syntaxHelp())).render()).append("\n");
            for (var line : handler.documentation()) {
                sb.append("    ").append(line).append("\n");
            }
        }

        DisplayData dd = DisplayData.withHtml(inspectHtml);
        dd.putText(sb.toString());
        return dd;
    }

    protected boolean canHandleOneLinePrefix(MagicSnippet magicSnippet, String prefix) {
        return magicSnippet.isLineMagic() && magicSnippet.lines().size() == 1 && magicSnippet.lines().get(0).code().startsWith(prefix);
    }
}
