package org.rapaio.jupyter.kernel.core.magic;

import java.util.List;

import org.rapaio.jupyter.kernel.core.display.DisplayData;

public class HelpMagicHandler implements MagicHandler {

    private static final String MAGIC_HELP_PREFIX_GENERIC = "%";
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
        return "% or %help";
    }

    @Override
    public List<String> helpMessage() {
        return List.of("Displays help about each magic command.");
    }

    @Override
    public boolean canHandleSnippet(String expr) {
        String text = expr.trim().toLowerCase();
        if (text.equals(MAGIC_HELP_PREFIX_GENERIC)) {
            return true;
        }
        return text.startsWith(MAGIC_HELP_PREFIX_FIXED);
    }

    @Override
    public Object eval(String expr) throws MagicParseException {
        String text = expr.trim().toLowerCase();
        if (text.equals(MAGIC_HELP_PREFIX_GENERIC)) {
            return help();
        }
        if (text.equals(MAGIC_HELP_PREFIX_FIXED)) {
            return help();
        }
        throw new MagicParseException(name(), expr, "Help magic syntax is incorrect.");
    }

    private DisplayData help() {
        StringBuilder sb = new StringBuilder();
        sb.append("Information about registered magic handlers.\n");
        for (var handler : magicHandlers) {
            sb.append("Magic handler: ").append(handler.name()).append("\n");
            sb.append("Syntax: ").append(handler.syntax()).append("\n");
            for (var helpLine : handler.helpMessage()) {
                sb.append("\t").append(helpLine).append("\n");
            }
        }
        DisplayData dd = new DisplayData();
        dd.putText(sb.toString());
        return dd;
    }
}
