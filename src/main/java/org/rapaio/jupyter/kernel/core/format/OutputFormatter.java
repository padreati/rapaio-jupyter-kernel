package org.rapaio.jupyter.kernel.core.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.java.CompilerException;
import org.rapaio.jupyter.kernel.core.java.EvaluationInterruptedException;
import org.rapaio.jupyter.kernel.core.java.EvaluationTimeoutException;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;

import jdk.jshell.DeclarationSnippet;
import jdk.jshell.EvalException;
import jdk.jshell.JShellException;
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
            exFormatters.put(EvalException.class, (JavaEngine engine, EvalException e) -> {
                String exceptionClassName = e.getExceptionClassName();
                StackTraceElement[] stackTrace = e.getStackTrace();
                JShellException shellException = e.getCause();

                List<String> output = new ArrayList<>();
                output.add(ANSI.start().bold().fgRed().text(exceptionClassName + ": " + e.getMessage() + "\n").build());
                if (shellException != null) {
                    output.add(ANSI.start().fgRed()
                            .text("Cause: " + shellException.getClass().getSimpleName() + ": " + shellException.getMessage()).build());
                }
                for (var stackElement : stackTrace) {
                    output.add(String.join("\n", ANSI.errorMessages("   at " + stackElement)));
                    if (stackElement.getFileName() != null && stackElement.getFileName().startsWith("#")) {
                        String id = stackElement.getFileName().substring(1);
                        Optional<Snippet> optional = engine.getShell().snippets().filter(s -> s.id().equals(id)).findAny();
                        optional.ifPresent(snippet -> output.addAll(ANSI.sourceCode(snippet.source(), stackElement.getLineNumber())));
                    }
                }
                return output;
            });
        }
    }

    private static List<String> genericFormatException(JavaEngine engine, Throwable throwable) {
        List<String> output = new ArrayList<>();
        while (true) {
            output.add(
                    ANSI.start().bold().fgRed().text(throwable.getClass().getSimpleName() + ": " + throwable.getMessage() + "\n").build());
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            for (var stackElement : stackTrace) {
                output.add(String.join("\n", ANSI.errorMessages("   at " + stackElement)));
                if (stackElement.getFileName() != null && stackElement.getFileName().startsWith("#")) {
                    String id = stackElement.getFileName().substring(1);
                    Optional<Snippet> optional = engine.getShell().snippets().filter(s -> s.id().equals(id)).findAny();
                    optional.ifPresent(snippet -> output.addAll(ANSI.sourceCode(snippet.source(), stackElement.getLineNumber())));
                }
            }
            Throwable th = throwable.getCause();
            if (th == null || th == throwable) {
                break;
            }
            throwable = th;
        }
        return output;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> List<String> exceptionFormat(JavaEngine javaEngine, T e) {
        ExceptionFormatter<T> formatter = (ExceptionFormatter<T>) exFormatters.get(e.getClass());
        if (formatter != null) {
            return formatter.format(javaEngine, e);
        }
        return genericFormatException(javaEngine, e);
    }
}
