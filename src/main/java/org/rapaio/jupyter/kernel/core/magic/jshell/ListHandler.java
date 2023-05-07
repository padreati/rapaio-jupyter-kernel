package org.rapaio.jupyter.kernel.core.magic.jshell;

import java.util.List;
import java.util.Optional;

import org.rapaio.jupyter.kernel.channels.ReplyEnv;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicEvaluator;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.handlers.JavaReplMagicHandler;

import jdk.jshell.Snippet;

public class ListHandler implements JShellCommandHandler {

    @Override
    public Object eval(MagicEvaluator magicEvaluator, JavaEngine javaEngine, ReplyEnv env, MagicSnippet magicSnippet, String line) throws
            MagicEvalException {
        String command = line.substring(JavaReplMagicHandler.LINE_PREFIX.length() + 1);
        String options = command.substring("/line".length()).trim();

        if (options.isEmpty()) {
            return evalSimpleList(javaEngine);
        }

        if (options.trim().equals("-all")) {
            return evalAllList(javaEngine);
        }

        return evalIdList(javaEngine, magicSnippet, options.trim());
    }

    private Object evalSimpleList(JavaEngine javaEngine) {
        List<Snippet> snippets = javaEngine.getShell().snippets()
                .filter(s -> javaEngine.getShell().status(s).isActive())
                .toList();
        StringBuilder sb = new StringBuilder();
        for (Snippet snippet : snippets) {
            sb.append(ANSI.start().bold().text("id:").fgGreen().text(snippet.id()).reset().text(" ")
                    .bold().text("type:").fgGreen().text(snippet.kind().name()).text("\n")
                    .build());
            for (String line : ANSI.sourceCode(snippet.source())) {
                sb.append(line).append("\n");
            }
            sb.append("\n");
        }
        return DisplayData.withText(sb.toString());
    }

    private Object evalAllList(JavaEngine javaEngine) {
        List<Snippet> snippets = javaEngine.getShell().snippets().toList();
        StringBuilder sb = new StringBuilder();
        for (Snippet snippet : snippets) {
            sb.append(ANSI.start().bold().text("id:").fgGreen().text(snippet.id()).reset().text(" ")
                    .bold().text("type:").fgGreen().text(snippet.kind().name()).text("\n")
                    .build());
            for (String line : ANSI.sourceCode(snippet.source())) {
                sb.append(line).append("\n");
            }
            sb.append("\n");
        }
        return DisplayData.withText(sb.toString());
    }

    private Object evalIdList(JavaEngine javaEngine, MagicSnippet magicSnippet, String id) throws
            MagicEvalException {
        Optional<Snippet> optional = javaEngine.getShell().snippets().filter(s -> s.id().equals(id.trim())).findAny();
        if (optional.isPresent()) {
            Snippet snippet = optional.get();
            StringBuilder sb = new StringBuilder();
            sb.append(ANSI.start().bold().text("id:").fgGreen().text(snippet.id()).reset().text(" ")
                    .bold().text("type:").fgGreen().text(snippet.kind().name()).text("\n")
                    .build());
            for (String line : ANSI.sourceCode(snippet.source())) {
                sb.append(line).append("\n");
            }
            sb.append("\n");
            return DisplayData.withText(sb.toString());
        }
        throw new MagicEvalException(magicSnippet, "No snippet with id: " + id + " was found.");
    }
}
