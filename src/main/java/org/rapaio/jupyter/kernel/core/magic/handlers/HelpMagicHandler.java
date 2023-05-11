package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.util.List;
import java.util.stream.Collectors;

import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicParseException;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.OneLineMagicHandler;

public class HelpMagicHandler implements MagicHandler {

    private static final String MAGIC_HELP_PREFIX_FIXED = "%help";

    private final List<MagicHandler> magicHandlers;

    public HelpMagicHandler(List<MagicHandler> magicHandlers) {
        this.magicHandlers = magicHandlers;
    }

    @Override
    public String name() {
        return "HelpHandler";
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
                        .documentation(List.of("Display help for all magic handlers."))
                        .canHandlePredicate(this::canHandleSnippet)
                        .evalFunction(this::evalLine)
                        .completeFunction((channels, magicSnippet) -> null)
                        .inspectFunction((channels, magicSnippet) -> null)
                        .build()
        );
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet snippet) {
        if (!snippet.oneLine() || snippet.lines().size() != 1) {
            return false;
        }
        String text = snippet.lines().get(0).code().trim().toLowerCase();
        return text.startsWith(MAGIC_HELP_PREFIX_FIXED);
    }

    private Object evalLine(MagicEngine magicEvaluator, JavaEngine javaEngine, Channels channels, MagicSnippet snippet) throws
            MagicParseException {
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

    @Override
    public CompleteMatches complete(Channels channels, MagicSnippet snippet) {
        return null;
    }

    private DisplayData help() {
        StringBuilder sb = new StringBuilder();
        sb.append(ANSI.start().bold().fgBlue().text("Information about registered magic handlers.\n").reset().build());
        for (var handler : magicHandlers) {

            sb.append("\n");
            sb.append(ANSI.start().bold().fgBlue().text(handler.name()).reset().build()).append("\n");

            sb.append(ANSI.start().bold().text("Documentation:\n").build());
            sb.append(handler.helpMessage().stream().map(s -> "    " + s).collect(Collectors.joining("\n")));

            sb.append(ANSI.start().bold().text("Syntax:\n").build());
            for (var oneLiner : handler.oneLineMagicHandlers()) {
                sb.append("    ").append(ANSI.start().bold().fgGreen().text(oneLiner.syntaxHelp()).build()).append("\n");
                sb.append("    ").append(String.join("\n", oneLiner.documentation())).append("\n");
            }
        }
        return DisplayData.withText(sb.toString());
    }
}
