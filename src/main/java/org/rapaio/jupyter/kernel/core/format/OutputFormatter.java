package org.rapaio.jupyter.kernel.core.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.java.CompilerException;
import org.rapaio.jupyter.kernel.core.java.EvaluationInterruptedException;
import org.rapaio.jupyter.kernel.core.java.EvaluationTimeoutException;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicParseException;

import jdk.jshell.DeclarationSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;

public final class OutputFormatter {

    private OutputFormatter() {
    }

    private static final Map<Class<? extends Exception>, ExceptionFormatter<? extends Exception>> exFormatters = new HashMap<>();

    static {
        {
            exFormatters.put(CompilerException.class, (JavaEngine engine, CompilerException e) -> {
                List<String> msgs = new ArrayList<>(ANSI.errorTypeHeader("Compile error"));
                SnippetEvent event = e.getBadSnippetCompilation();
                Snippet snippet = event.snippet();
                var diagnostics = engine.getShell().diagnostics(snippet).toList();
                for (var d : diagnostics) {
                    msgs.addAll(ANSI.sourceCode(snippet.source(), (int) d.getPosition(),
                            (int) d.getStartPosition(), (int) d.getEndPosition()));

                    msgs.addAll(ANSI.errorMessages(d.getMessage(Locale.getDefault())));
                    msgs.add("");
                }
                // Declaration snippets are unique in that they can be active with unresolved references
                if (snippet instanceof DeclarationSnippet declarationSnippet) {
                    List<String> unresolvedDependencies = engine.getShell().unresolvedDependencies(declarationSnippet).toList();
                    if (!unresolvedDependencies.isEmpty()) {
                        msgs.addAll(ANSI.sourceCode(snippet.source()));
                        msgs.addAll(ANSI.errorMessages("Unresolved dependencies:"));
                        unresolvedDependencies.forEach(dep -> msgs.addAll(ANSI.errorMessages("   - " + dep)));
                    }
                }
                return msgs;
            });
            exFormatters.put(EvaluationInterruptedException.class, (JavaEngine engine, EvaluationInterruptedException e) -> {
                List<String> msgs = new ArrayList<>(ANSI.errorTypeHeader("InterruptedException"));
                msgs.addAll(ANSI.sourceCode(e.getSource()));
                return msgs;
            });
            exFormatters.put(EvaluationTimeoutException.class, (JavaEngine engine, EvaluationInterruptedException e) -> {
                List<String> msgs = new ArrayList<>(ANSI.errorTypeHeader("TimeoutException"));
                msgs.addAll(ANSI.sourceCode(e.getSource()));
                msgs.addAll(ANSI.errorMessages(e.getMessage()));
                return msgs;
            });
            exFormatters.put(MagicParseException.class, (JavaEngine engine, MagicParseException e) -> {
                // todo: something better
                return List.of(e.getMessage());
            });
            exFormatters.put(MagicEvalException.class, (JavaEngine engine, MagicEvalException e) -> {
                // todo: something better
                return List.of(e.getMessage());
            });
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Exception> List<String> exceptionFormat(JavaEngine javaEngine, T e) {
        ExceptionFormatter<T> formatter = (ExceptionFormatter<T>) exFormatters.get(e.getClass());
        if (formatter != null) {
            return formatter.format(javaEngine, e);
        }
        return List.of(e.getMessage());
    }
}
