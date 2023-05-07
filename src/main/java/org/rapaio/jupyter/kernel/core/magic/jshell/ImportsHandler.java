package org.rapaio.jupyter.kernel.core.magic.jshell;

import java.util.List;

import org.rapaio.jupyter.kernel.channels.ReplyEnv;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicEvaluator;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.handlers.JavaReplMagicHandler;

import jdk.jshell.ImportSnippet;

public class ImportsHandler  implements JShellCommandHandler {

    @Override
    public Object eval(MagicEvaluator magicEvaluator, JavaEngine javaEngine, ReplyEnv env, MagicSnippet magicSnippet, String line) throws
            MagicEvalException {
        String command = line.substring(JavaReplMagicHandler.LINE_PREFIX.length() + 1);
        String options = command.substring("/imports".length()).trim();

        if (!options.isEmpty()) {
            throw new MagicEvalException(magicSnippet, "%jshell /vars command does not have arguments.");
        }

        List<ImportSnippet> snippets = javaEngine.getShell().imports()
                .filter(s -> javaEngine.getShell().status(s).isActive())
                .toList();
        StringBuilder sb = new StringBuilder();
        for (ImportSnippet snippet : snippets) {
            for(String sourceLine : ANSI.sourceCode((snippet.isStatic() ? "static " : "") + snippet.fullname())) {
                sb.append(sourceLine).append("\n");
            }
        }
        return DisplayData.withText(sb.toString());
    }
}
