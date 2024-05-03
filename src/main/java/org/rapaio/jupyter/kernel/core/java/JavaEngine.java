package org.rapaio.jupyter.kernel.core.java;

import static org.rapaio.jupyter.kernel.core.display.html.Tags.b;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.br;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.each;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.iif;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.join;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.p;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.texts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.ExecutionContext;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.html.JavadocTools;
import org.rapaio.jupyter.kernel.core.display.html.Tag;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.java.io.JShellConsole;
import org.rapaio.jupyter.kernel.message.messages.ShellIsCompleteReply;

import jdk.jshell.EvalException;
import jdk.jshell.JShell;
import jdk.jshell.JShellException;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;

public class JavaEngine {

    private static final Logger logger = Logger.getLogger(Logger.class.getSimpleName());

    private static final EnumSet<Snippet.SubKind> ALLOWED_LAST_OUTPUT = EnumSet.of(
            Snippet.SubKind.VAR_VALUE_SUBKIND,
            Snippet.SubKind.OTHER_EXPRESSION_SUBKIND,
            Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND
    );

    private final String executionId;
    private final RapaioExecutionControlProvider controlProvider;
    private final JShell shell;
    private final SourceCodeAnalysis sourceAnalysis;
    private final List<String> startupScripts;

    protected JavaEngine(String executionId, RapaioExecutionControlProvider controlProvider, JShell shell, List<String> startupScripts) {
        this.executionId = executionId;
        this.controlProvider = controlProvider;
        this.shell = shell;
        this.sourceAnalysis = shell.sourceCodeAnalysis();
        this.startupScripts = startupScripts;
    }

    public JShell getShell() {
        return shell;
    }

    public SourceCodeAnalysis getSourceAnalysis() {
        return sourceAnalysis;
    }

    public void initialize() {
        startupScripts.forEach(line -> {
            var events = shell.eval(line);
            for (var event : events) {
                if (event.status() == Snippet.Status.REJECTED) {
                    logger.severe(() -> shell.diagnostics(event.snippet()).map(diag -> diag.getMessage(Locale.getDefault())).collect(
                            Collectors.joining(";")));
                    throw new IllegalStateException(event.exception());
                }
            }
        });
    }

    /**
     * Called to interrupt the current execution.
     */
    public void interrupt() {
        controlProvider.getRegisteredControl(executionId).interrupt();
        controlProvider.getRegisteredControl(executionId).stop();
        shell.stop();
    }

    /**
     * Called to stop the execution.
     * If restart is true, then we can save some resources since it will start again.
     */
    public void shutdown() {
        shell.close();
    }

    public Object eval(ExecutionContext context, String code) throws Exception {

        // the last result will be displayed if we have the result of an expression,
        // or it's the value of a variable (emulate the behavior from ipython)
        Object lastResult = null;
        SourceCodeAnalysis.CompletionInfo info = sourceAnalysis.analyzeCompletion(code);

        while (info.completeness().isComplete()) {
            // pay attention to trim, new lines can broke code evaluation
            String trimmedSource = info.source().trim();
            if (!trimmedSource.isEmpty()) {
                lastResult = evalSnippet(trimmedSource);
            }
            info = sourceAnalysis.analyzeCompletion(info.remaining());
        }

        // ignore eventual last empty snippet, but throw error if not empty
        if (info.completeness() != SourceCodeAnalysis.Completeness.EMPTY) {
            throw new IncompleteSourceException(info.remaining().trim());
        }

        return lastResult;
    }

    protected Object evalSnippet(String code) throws Exception {
        RapaioExecutionControl control = controlProvider.getRegisteredControl(executionId);

        List<SnippetEvent> events = shell.eval(code);

        Object result = null;

        for (SnippetEvent event : events) {
            String key = event.value();
            if (key == null) {
                continue;
            }
            Snippet.SubKind subKind = event.snippet().subKind();
            Object value = subKind.isExecutable() ? control.takeResult(key) : event.value();
            result = (ALLOWED_LAST_OUTPUT.contains(subKind)) ? value : null;
        }

        for (SnippetEvent event : events) {
            if (event.causeSnippet() == null) {
                JShellException e = event.exception();
                if (e != null) {
                    if (e instanceof EvalException) {
                        switch (((EvalException) e).getExceptionClassName()) {
                            case RapaioExecutionControl.TIMEOUT_MARKER ->
                                    throw new EvaluationTimeoutException(control.getTimeout(), code.trim());
                            case RapaioExecutionControl.INTERRUPTED_MARKER -> throw new EvaluationInterruptedException(code.trim());
                            default -> throw e;
                        }
                    }

                    throw e;
                }
                if (!event.status().isDefined()) {
                    throw new CompilerException(event);
                }
            }
        }

        return result;
    }

    public IsCompleteResult isComplete(String code) {
        SourceCodeAnalysis.CompletionInfo info = sourceAnalysis.analyzeCompletion(code);
        while (info.completeness().isComplete()) {
            info = sourceAnalysis.analyzeCompletion(info.remaining());
        }

        return switch (info.completeness()) {
            case COMPLETE, COMPLETE_WITH_SEMI, EMPTY -> new IsCompleteResult(ShellIsCompleteReply.Status.COMPLETE);
            case UNKNOWN -> new IsCompleteResult(ShellIsCompleteReply.Status.INVALID);
            case CONSIDERED_INCOMPLETE, DEFINITELY_INCOMPLETE ->
                    new IsCompleteResult(ShellIsCompleteReply.Status.INCOMPLETE, computeIndent(info));
        };
    }

    private String computeIndent(SourceCodeAnalysis.CompletionInfo info) {
        // TODO: use info to compute indent
        return "";
    }

    public CompleteMatches complete(String code, int at) {
        int[] anchor = new int[1];
        List<SourceCodeAnalysis.Suggestion> suggestions = sourceAnalysis.completionSuggestions(code, at, anchor);
        if (suggestions == null || suggestions.isEmpty()) {
            return null;
        }
        suggestions = new ArrayList<>(suggestions);
        suggestions.sort(Comparator.comparingInt(s -> s.continuation().length()));
        suggestions.sort(Comparator.comparing(s -> s.matchesType() ? 0 : 1));

        List<String> options = suggestions.stream()
                .map(SourceCodeAnalysis.Suggestion::continuation)
                .distinct()
                .collect(Collectors.toList());

        return new CompleteMatches(options, anchor[0], at);
    }

    public DisplayData inspect(String source, int pos) {

        while (pos + 1 < source.length() && Character.isJavaIdentifierPart(source.charAt(pos + 1))) {
            pos++;
        }

        int parenIdx = pos;
        while (parenIdx + 1 < source.length() && Character.isWhitespace(source.charAt(parenIdx + 1))) {
            parenIdx++;
        }
        if (parenIdx + 1 < source.length() && source.charAt(parenIdx + 1) == '(') {
            pos = parenIdx + 1;
        }

        List<SourceCodeAnalysis.Documentation> documentations = sourceAnalysis.documentation(source, pos + 1, true);
        if (documentations == null || documentations.isEmpty()) {
            return null;
        }

        String html = join(
                each(documentations, doc -> p(join(
                                b(texts(doc.signature())),
                                iif(doc.javadoc() != null, () -> new Tag[] {br(), texts(JavadocTools.javadocPreprocess(doc.javadoc()))})
                        )
                ))
        ).render();
        StringBuilder sb = new StringBuilder();
        for (var doc : documentations) {
            sb.append(ANSI.start().bold().text(doc.signature()).render()).append("\n");
            if (doc.javadoc() != null) {
                sb.append(doc.javadoc()).append("\n");
            }
            sb.append("\n");
        }

        DisplayData dd = DisplayData.withHtml(html);
        dd.putText(sb.toString());
        return dd;
    }

    public static Builder builder(JShellConsole shellConsole) {
        return new Builder(shellConsole);
    }

    public static class Builder {

        private final JShellConsole shellConsole;

        private long timeoutMillis = -1L;
        private final List<String> compilerOptions = new LinkedList<>();
        private final List<String> startupScripts = new LinkedList<>();

        protected Builder(JShellConsole shellConsole) {
            this.shellConsole = shellConsole;
            this.compilerOptions.addAll(List.of("--enable-preview", "--release", "22"));
        }

        public Builder withTimeoutMillis(Long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            return this;
        }

        public Builder withCompilerOptions(Collection<String> options) {
            this.compilerOptions.addAll(options);
            return this;
        }

        public Builder withStartupScript(String script) {
            if (script != null) {
                this.startupScripts.add(script);
            }
            return this;
        }

        public Builder withStartupScript(InputStream in) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line).append("\n");
                }
                this.startupScripts.add(sb.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return this;
        }

        public JavaEngine build() {

            String executionId = UUID.randomUUID().toString();

            Map<String, String> controlParameterMap = new HashMap<>();
            controlParameterMap.put(RapaioExecutionControlProvider.EXECUTION_ID_KEY, executionId);
            controlParameterMap.put(RapaioExecutionControlProvider.EXECUTION_TIMEOUT_KEY, String.valueOf(timeoutMillis));

            RapaioExecutionControlProvider controlProvider = new RapaioExecutionControlProvider();
            JShell shell = JShell.builder()
                    .compilerOptions(compilerOptions.toArray(String[]::new))
                    .executionEngine(controlProvider, controlParameterMap)
                    .in(shellConsole.getIn())
                    .out(new PrintStream(shellConsole.getOut(), true))
                    .err(new PrintStream(shellConsole.getErr(), true))
                    .build();
            return new JavaEngine(executionId, controlProvider, shell, startupScripts);
        }
    }
}
