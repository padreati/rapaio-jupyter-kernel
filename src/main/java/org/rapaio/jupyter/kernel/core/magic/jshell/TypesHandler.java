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

import jdk.jshell.TypeDeclSnippet;

public class TypesHandler implements JShellCommandHandler {

    @Override
    public Object eval(MagicEvaluator magicEvaluator, JavaEngine javaEngine, ReplyEnv env, MagicSnippet magicSnippet, String line) throws
            MagicEvalException {
        String command = line.substring(JavaReplMagicHandler.LINE_PREFIX.length() + 1);
        String options = command.substring("/types".length()).trim();

        if (!options.isEmpty()) {
            throw new MagicEvalException(magicSnippet, "%jshell /vars command does not have arguments.");
        }

        List<TypeDeclSnippet> snippets = javaEngine.getShell().types()
                .filter(s -> javaEngine.getShell().status(s).isActive())
                .toList();
        StringBuilder sb = new StringBuilder();
        for (TypeDeclSnippet snippet : snippets) {
            String type = switch (snippet.subKind()) {
                case CLASS_SUBKIND -> "class";
                case INTERFACE_SUBKIND -> "interface";
                case RECORD_SUBKIND -> "record";
                case ENUM_SUBKIND -> "enum";
                default -> "unknown";
            };
            List<String> sourceLines = ANSI.sourceCode(type + " " + snippet.name());
            for (String sourceLine : sourceLines) {
                sb.append(sourceLine).append("\n");
            }
        }
        return DisplayData.withText(sb.toString());
    }
}
