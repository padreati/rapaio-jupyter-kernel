package org.rapaio.jupyter.kernel.core.magic.handlers;

import static org.rapaio.jupyter.kernel.core.display.html.Tags.each;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.p;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.texts;

import java.util.List;

import org.rapaio.jupyter.kernel.channels.ReplyEnv;
import org.rapaio.jupyter.kernel.core.ReplacementOptions;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicParseException;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;

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
    public String syntax() {
        return "%help";
    }

    @Override
    public List<String> helpMessage() {
        return List.of("Displays help about each magic command.");
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet snippet) {
        if (!snippet.oneLine() || snippet.lines().size() != 1) {
            return false;
        }
        String text = snippet.lines().get(0).code().trim().toLowerCase();
        return text.startsWith(MAGIC_HELP_PREFIX_FIXED);
    }

    @Override
    public Object eval(JavaEngine javaEngine, ReplyEnv env, MagicSnippet snippet) throws MagicParseException {
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
    public ReplacementOptions complete(ReplyEnv env, MagicSnippet snippet) {
        return null;
    }

    private DisplayData help() {
        StringBuilder sb = new StringBuilder();
        sb.append(ANSI.start().bold().fgBlue().text("Information about registered magic handlers.\n").reset().build());
        for (var handler : magicHandlers) {
            sb.append("\n");
            sb.append(ANSI.start().bold().text(handler.name()).reset().build()).append("\n");

            sb.append("Syntax: ");
            sb.append(ANSI.start().bold().fgGreen().text(handler.syntax()).reset().build()).append("\n");
            for (var helpLine : handler.helpMessage()) {
                sb.append("\t").append(helpLine).append("\n");
            }
        }
        return DisplayData.withText(sb.toString());
    }
}
