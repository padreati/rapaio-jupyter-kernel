package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.util.Arrays;
import java.util.List;

import org.rapaio.jupyter.kernel.channels.ReplyEnv;
import org.rapaio.jupyter.kernel.core.ReplacementOptions;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;

public class JavaReplMagicHandler implements MagicHandler {

    private static final String LINE_PREFIX = "%jshell";

    @Override
    public String name() {
        return "JShell (non implemented yet!)";
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
    public boolean canHandleSnippet(MagicSnippet snippet) {
        if (snippet == null || !snippet.oneLine() || snippet.lines().size() != 1 || snippet.lines().get(0).code().trim().isEmpty()) {
            return false;
        }
        String expr = snippet.lines().get(0).code();
        List<String> tokens = Arrays.stream(expr.trim().split("\\s")).filter(s -> !s.isEmpty()).toList();
        if (tokens.isEmpty()) {
            return false;
        }
        return LINE_PREFIX.equals(tokens.get(0));
    }

    @Override
    public Object eval(JavaEngine javaEngine, ReplyEnv env, MagicSnippet snippet) {
        if (!canHandleSnippet(snippet)) {
            throw new RuntimeException("Try to execute a magic snippet to improper handler.");
        }
        String expr = snippet.lines().get(0).code();
        List<String> tokens = Arrays.stream(expr.trim().split("\\s")).filter(s -> !s.isEmpty()).toList();
        tokens = tokens.subList(1, tokens.size());


        return null;
    }

    @Override
    public ReplacementOptions complete(ReplyEnv env, MagicSnippet snippet) {
        return null;
    }

    @Override
    public DisplayData inspect(ReplyEnv env, MagicSnippet snippet) {
        return null;
    }
}
