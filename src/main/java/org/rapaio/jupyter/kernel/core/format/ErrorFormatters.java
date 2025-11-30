package org.rapaio.jupyter.kernel.core.format;

import jdk.jshell.*;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.java.CompilerException;
import org.rapaio.jupyter.kernel.core.java.EvaluationInterruptedException;
import org.rapaio.jupyter.kernel.core.java.EvaluationTimeoutException;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;

import java.util.*;

public final class ErrorFormatters {

    private ErrorFormatters() {
    }

    private static final Map<Class<? extends Throwable>, ErrorFormatter<? extends Throwable>> map = new HashMap<>();

    private static <E extends Throwable> void register(Class<E> clazz, ErrorFormatter<E> format) {
        map.put(clazz, format);
    }

    static {

        register(CompilerException.class, (kernel, e) -> {
            List<String> msgs = new ArrayList<>(ANSI.errorTypeHeader("Compile error:"));
            SnippetEvent event = e.badSnippet();
            Snippet snippet = event.snippet();
            var diagnostics = kernel.javaEngine().getShell().diagnostics(snippet).toList();
            for (var d : diagnostics) {
                msgs.addAll(ANSI.sourceCode(snippet.source(), (int) d.getPosition(),
                        (int) d.getStartPosition(), (int) d.getEndPosition()));

                msgs.addAll(ANSI.errorMessages(d.getMessage(Locale.getDefault())));
                msgs.add("");
            }
            // Declaration snippets are unique in that they can be active with unresolved references
            if (snippet instanceof DeclarationSnippet declarationSnippet) {
                List<String> unresolvedDependencies = kernel.javaEngine().getShell().unresolvedDependencies(declarationSnippet).toList();
                if (!unresolvedDependencies.isEmpty()) {
                    msgs.addAll(ANSI.sourceCode(snippet.source()));
                    msgs.addAll(ANSI.errorMessages("Unresolved dependencies:"));
                    unresolvedDependencies.forEach(dep -> msgs.addAll(ANSI.errorMessages("   - " + dep)));
                }
            }
            return msgs;
        });

        register(EvaluationInterruptedException.class, (kernel, e) -> {
            List<String> msgs = new ArrayList<>(ANSI.errorTypeHeader("InterruptedException:"));
            msgs.addAll(ANSI.sourceCode(e.getSource()));
            return msgs;
        });

        register(EvaluationTimeoutException.class, (kernel, e) -> {
            List<String> msgs = new ArrayList<>(ANSI.errorTypeHeader("TimeoutException:"));
            msgs.addAll(ANSI.sourceCode(e.getSource()));
            msgs.addAll(ANSI.errorMessages(e.getMessage()));
            return msgs;
        });

        register(EvalException.class, (kernel, e) -> {
            String exceptionClassName = e.getExceptionClassName();
            StackTraceElement[] stackTrace = e.getStackTrace();
            JShellException shellException = e.getCause();

            List<String> output = new ArrayList<>();
            output.add(ANSI.start().bold().fgRed().text(exceptionClassName + ": " + e.getMessage() + "\n").render());
            if (shellException != null) {
                output.add(ANSI.start().fgRed()
                        .text("Cause: " + shellException.getClass().getSimpleName() + ": " + shellException.getMessage()).render());
            }
            for (var stackElement : stackTrace) {
                output.add(String.join("\n", ANSI.errorMessages("   at " + stackElement)));
                if (stackElement.getFileName() != null && stackElement.getFileName().startsWith("#")) {
                    String id = stackElement.getFileName().substring(1);
                    Optional<Snippet> optional = kernel.javaEngine().getShell().snippets().filter(s -> s.id().equals(id)).findAny();
                    optional.ifPresent(snippet -> output.addAll(ANSI.sourceCode(snippet.source(), stackElement.getLineNumber())));
                }
            }
            return output;
        });
        register(MagicEvalException.class, (kernel, e) -> {
            List<String> output = new ArrayList<>(ANSI.errorTypeHeader("MagicEvalException:"));
            if (e.hasErrorLine()) {
                var magicSnippet = e.magicSnippet();
                var code = magicSnippet.line(e.errorLine()).code();
                output.addAll(ANSI.sourceCode(code, e.errorStart(), e.errorStart(), e.errorEnd()));
            }
            output.addAll(ANSI.errorMessages(e.getMessage()));
            return output;
        });
    }

    private static List<String> genericFormatException(RapaioKernel kernel, Throwable throwable) {
        List<String> output = new ArrayList<>();
        while (true) {
            output.add(
                    ANSI.start().bold().fgRed().text(throwable.getClass().getSimpleName() + ": " + throwable.getMessage() + "\n").render());
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            for (var stackElement : stackTrace) {
                output.add(String.join("\n", ANSI.errorMessages("   at " + stackElement)));
                if (stackElement.getFileName() != null && stackElement.getFileName().startsWith("#")) {
                    String id = stackElement.getFileName().substring(1);
                    Optional<Snippet> optional = kernel.javaEngine().getShell().snippets().filter(s -> s.id().equals(id)).findAny();
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
    public static <T extends Throwable> List<String> exceptionFormat(RapaioKernel kernel, T e) {
        ErrorFormatter<T> formatter = (ErrorFormatter<T>) map.get(e.getClass());
        if (formatter != null) {
            return formatter.format(kernel, e);
        }
        return genericFormatException(kernel, e);
    }
}
