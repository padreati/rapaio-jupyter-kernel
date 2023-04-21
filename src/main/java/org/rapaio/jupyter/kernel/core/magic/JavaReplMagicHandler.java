package org.rapaio.jupyter.kernel.core.magic;

import java.util.Arrays;
import java.util.List;

public class JavaReplMagicHandler implements MagicHandler {

    private static final String LINE_PREFIX = "%jshell";

    @Override
    public String name() {
        return "JShell";
    }

    @Override
    public String syntax() {
        return "%jshell /cmd";
    }

    @Override
    public List<String> helpMessage() {
        return List.of("Runs the given command against JShell REPL and displays the results.");
    }

    @Override
    public boolean canHandleSnippet(String expr) {
        if (expr == null || expr.trim().isEmpty()) {
            return false;
        }
        List<String> tokens = Arrays.stream(expr.trim().split("\\s")).filter(s -> !s.isEmpty()).toList();
        if (tokens.isEmpty()) {
            return false;
        }
        return LINE_PREFIX.equals(tokens.get(0));
    }

    @Override
    public Object eval(String expr) throws MagicParseException {
        if (!canHandleSnippet(expr)) {
            throw new MagicParseException(name(), expr, "Request to evaluate a snippet which cannot be handled");
        }

        List<String> tokens = Arrays.stream(expr.trim().split("\\s")).filter(s -> !s.isEmpty()).toList();
        tokens = tokens.subList(1, tokens.size());


        return null;
    }
}
