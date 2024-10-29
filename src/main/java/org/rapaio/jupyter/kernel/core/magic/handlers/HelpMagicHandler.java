package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.util.List;
import java.util.stream.Collectors;

import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.SnippetMagicHandler;

public class HelpMagicHandler extends MagicHandler {

    private static final String ONE_LINE_PREFIX = "%help";

    private final List<MagicHandler> magicHandlers;

    public HelpMagicHandler(List<MagicHandler> magicHandlers) {
        this.magicHandlers = magicHandlers;
    }

    @Override
    public String name() {
        return "Help";
    }

    @Override
    public List<String> helpMessage() {
        return List.of("Magic which displays help for all the magic tools.");
    }

    @Override
    public List<SnippetMagicHandler> snippetMagicHandlers() {
        return List.of(
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher("%help")
                        .syntaxHelp(List.of("%help"))
                        .syntaxPrefix("%help")
                        .documentation(List.of("Display help for all magic handlers."))
                        .canHandlePredicate(this::canHandleSnippet)
                        .evalFunction(this::evalLine)
                        .build()
        );
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet magicSnippet) {
        return canHandleOneLinePrefix(magicSnippet, ONE_LINE_PREFIX);
    }

    Object evalLine(RapaioKernel kernel, MagicSnippet snippet) throws MagicEvalException {
        if (!canHandleSnippet(snippet)) {
            throw new MagicEvalException(snippet, "Try to execute a magic snippet to improper handler.");
        }
        String expr = snippet.lines().get(0).code();
        String text = expr.trim().toLowerCase();

        if (text.equals(ONE_LINE_PREFIX)) {
            return help();
        }
        throw new MagicEvalException(snippet, "Help magic syntax is incorrect.");
    }

    private DisplayData help() {
        StringBuilder sb = new StringBuilder();
        sb.append(ANSI.start().bold().fgBlue().text("Information about registered magic handlers.\n").reset().render());
        for (var handler : magicHandlers) {

            sb.append("\n");
            sb.append(ANSI.start().bold().fgBlue().text(handler.name()).reset().render()).append("\n");

            sb.append(ANSI.start().bold().text("Documentation:\n").render());
            sb.append(handler.helpMessage().stream().map(s -> "    " + s).collect(Collectors.joining("\n"))).append("\n");

            sb.append(ANSI.start().bold().text("Syntax:\n").render());
            for (var snippetMagicHandler : handler.snippetMagicHandlers()) {
                for(var line : snippetMagicHandler.syntaxHelp()) {
                    sb.append("    ").append(ANSI.start().bold().fgGreen().text(line).render()).append("\n");
                }
                sb.append("    ").append(String.join("\n", snippetMagicHandler.documentation())).append("\n");
            }
        }
        return DisplayData.withText(sb.toString());
    }
}
