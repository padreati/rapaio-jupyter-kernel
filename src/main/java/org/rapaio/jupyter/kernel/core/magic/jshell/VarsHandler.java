package org.rapaio.jupyter.kernel.core.magic.jshell;

import java.util.List;

import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.handlers.JavaReplMagicHandler;

import jdk.jshell.VarSnippet;

public class VarsHandler implements JShellCommandHandler {

    @Override
    public Object eval(MagicEngine magicEvaluator, JavaEngine javaEngine, Channels channels, MagicSnippet magicSnippet, String line) throws
            MagicEvalException {
        String command = line.substring(JavaReplMagicHandler.LINE_PREFIX.length() + 1);
        String options = command.substring("/vars".length()).trim();

        if (!options.isEmpty()) {
            throw new MagicEvalException(magicSnippet, "%jshell /vars command does not have arguments.");
        }

        List<VarSnippet> snippets = javaEngine.getShell().variables()
                .filter(s -> javaEngine.getShell().status(s).isActive())
                .toList();
        StringBuilder sb = new StringBuilder();
        for (VarSnippet snippet : snippets) {
            sb.append(ANSI.start().text("|    ").bold().text(snippet.typeName() + " ").fgGreen().text(snippet.name())
                    .reset().text(" = ")
                    .reset().text(javaEngine.getShell().varValue(snippet)).text("\n")
                    .build());
        }
        return DisplayData.withText(sb.toString());
    }
}

