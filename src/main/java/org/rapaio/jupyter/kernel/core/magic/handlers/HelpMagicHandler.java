package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.util.List;
import java.util.stream.Collectors;

import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicParseException;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.OneLineMagicHandler;

public class HelpMagicHandler extends MagicHandler {

    private static final String MAGIC_HELP_PREFIX_FIXED = "%help";

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
    public List<OneLineMagicHandler> oneLineMagicHandlers() {
        return List.of(
                OneLineMagicHandler.builder()
                        .syntaxMatcher("%help")
                        .syntaxHelp("%help")
                        .syntaxPrefix("%help")
                        .documentation(List.of("Display help for all magic handlers."))
                        .canHandlePredicate(this::canHandleSnippet)
                        .evalFunction(this::evalLine)
                        .build()
        );
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet magicSnippet) {
        if (!magicSnippet.oneLine() || magicSnippet.lines().size() != 1) {
            return false;
        }
        String text = magicSnippet.lines().get(0).code().trim().toLowerCase();
        return text.startsWith(MAGIC_HELP_PREFIX_FIXED);
    }

    Object evalLine(RapaioKernel kernel, MagicSnippet snippet) throws MagicParseException {
        if (!canHandleSnippet(snippet)) {
            throw new RuntimeException("Try to execute a magic snippet to improper handler.");
        }
        String expr = snippet.lines().get(0).code();
        String text = expr.trim().toLowerCase();

        if (text.equals(MAGIC_HELP_PREFIX_FIXED)) {
            return help();
        }
        throw new MagicParseException(name(), expr, "Help magic syntax is incorrect.");
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
            for (var oneLiner : handler.oneLineMagicHandlers()) {
                sb.append("    ").append(ANSI.start().bold().fgGreen().text(oneLiner.syntaxHelp()).render()).append("\n");
                sb.append("    ").append(String.join("\n", oneLiner.documentation())).append("\n");
            }
        }
        return DisplayData.withText(sb.toString());
    }
}
