package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.jshell.ImportsHandler;
import org.rapaio.jupyter.kernel.core.magic.jshell.JShellCommandHandler;
import org.rapaio.jupyter.kernel.core.magic.jshell.ListHandler;
import org.rapaio.jupyter.kernel.core.magic.jshell.MethodsHandler;
import org.rapaio.jupyter.kernel.core.magic.jshell.TypesHandler;
import org.rapaio.jupyter.kernel.core.magic.jshell.VarsHandler;

public class JavaReplMagicHandler implements MagicHandler {

    public static final String LINE_PREFIX = "%jshell";

    private static final Map<String, JShellCommandHandler> handlerMap = new LinkedHashMap<>();

    static {
        ListHandler listHandler = new ListHandler();
        handlerMap.put("%jshell /list", listHandler);
        handlerMap.put("%jshell /list -all", listHandler);
        handlerMap.put("%jshell /list \\w*", listHandler);
        handlerMap.put("%jshell /vars", new VarsHandler());
        handlerMap.put("%jshell /imports", new ImportsHandler());
        handlerMap.put("%jshell /types", new TypesHandler());
        handlerMap.put("%jshell /methods", new MethodsHandler());
    }

    @Override
    public String name() {
        return "JShell (non implemented yet!)";
    }

    @Override
    public List<String> syntax() {
        return List.of(
                "%jshell /list",
                "%jshell /list -all",
                "%jshell /list [id]",
                "%jshell /vars",
                "%jshell /imports",
                "%jshell /types",
                "%jshell /methods"
        );
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
    public Object eval(MagicEngine magicEvaluator, JavaEngine javaEngine, Channels channels, MagicSnippet snippet) throws MagicEvalException {
        if (!canHandleSnippet(snippet)) {
            throw new RuntimeException("Try to execute a magic snippet to improper handler.");
        }
        String expr = snippet.lines().get(0).code().trim();
        for (var entry : handlerMap.entrySet()) {
            if (expr.trim().matches(entry.getKey())) {
                return entry.getValue().eval(magicEvaluator, javaEngine, channels, snippet, expr.trim());
            }
        }
        channels.writeToStdErr("Command not executed either because there is no handler or due to a syntax error.");
        return null;
    }

    @Override
    public CompleteMatches complete(Channels channels, MagicSnippet snippet) {
        return null;
    }
}
