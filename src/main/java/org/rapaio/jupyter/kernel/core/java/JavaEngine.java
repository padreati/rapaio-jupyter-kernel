package org.rapaio.jupyter.kernel.core.java;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.DisplayData;

import jdk.jshell.EvalException;
import jdk.jshell.JShell;
import jdk.jshell.JShellException;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;

public class JavaEngine {

    private final String executionId;
    private final RapaioExecutionControlProvider controlProvider;
    private final JShell shell;
    private final SourceCodeAnalysis sourceAnalysis;

    protected JavaEngine(String executionId, RapaioExecutionControlProvider controlProvider, JShell shell) {
        this.executionId = executionId;
        this.controlProvider = controlProvider;
        this.shell = shell;
        this.sourceAnalysis = shell.sourceCodeAnalysis();
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

    public Object eval(String code) throws Exception {

        Object lastResult = null;
        SourceCodeAnalysis.CompletionInfo info = sourceAnalysis.analyzeCompletion(code);

        while (info.completeness().isComplete()) {
            lastResult = evalSnippet(info.source());
            info = sourceAnalysis.analyzeCompletion(info.remaining());
        }

        // ignore eventual last empty snippet
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

            result = subKind.isExecutable() ? control.takeResult(key) : event.value();
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
                    throw new CompileException(event);
                }
            }
        }

        return result;
    }

    public String isComplete(String code) {
        SourceCodeAnalysis.CompletionInfo info = sourceAnalysis.analyzeCompletion(code);
        while (info.completeness().isComplete()) {
            info = sourceAnalysis.analyzeCompletion(info.remaining());
        }

        return switch (info.completeness()) {
            case COMPLETE, COMPLETE_WITH_SEMI, EMPTY -> RapaioKernel.IS_COMPLETE_STATUS_YES;
            case UNKNOWN -> RapaioKernel.IS_COMPLETE_STATUS_BAD;
            case CONSIDERED_INCOMPLETE, DEFINITELY_INCOMPLETE ->
                // TODO: improvement on how to compute indentation on info.remaining()
                    "0";
        };
    }

    public DisplayData inspect(String source, int pos, int detailLevel) {

        // Move the code position to the end of the identifier to make the inspection work at any
        // point in the identifier. i.e "System.o|ut" or "System.out|" will return the same result.
        while (pos + 1 < source.length() && Character.isJavaIdentifierPart(source.charAt(pos + 1))) {
            pos++;
        }

        // If the next non-whitespace character is an opening paren '(' then this must be included
        // in the documentation search to ensure it searches for a method call.
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

        StringBuilder sb = new StringBuilder();
        for (var doc : documentations) {
            sb.append(doc.signature());

            String javadoc = doc.javadoc();
            if (javadoc != null) {
                sb.append("\n").append(javadoc);
            }
            sb.append("\n\n");
        }
        return new DisplayData(sb.toString());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long timeoutMillis = null;
        private final List<String> compilerOptions = new LinkedList<>();

        public Builder withTimeoutMillis(Long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            return this;
        }

        public Builder withCompilerOption(String option) {
            this.compilerOptions.add(option);
            return this;
        }

        public Builder withCompilerOptions(Collection<String> options) {
            this.compilerOptions.addAll(options);
            return this;
        }

        private void validateValues() {
            if (timeoutMillis == null) {
                throw new IllegalArgumentException("Timeout must be specified.");
            }
        }

        public JavaEngine build() {

            validateValues();

            String executionId = UUID.randomUUID().toString();

            Map<String, String> controlParameterMap = new HashMap<>();
            controlParameterMap.put(RapaioExecutionControlProvider.EXECUTION_ID_KEY, executionId);
            controlParameterMap.put(RapaioExecutionControlProvider.EXECUTION_TIMEOUT_MILLIS_KEY, String.valueOf(timeoutMillis));

            RapaioExecutionControlProvider controlProvider = new RapaioExecutionControlProvider();
            JShell shell = JShell.builder()

                    .compilerOptions(compilerOptions.toArray(String[]::new))
                    .executionEngine(controlProvider, controlParameterMap)
                    .build();
            return new JavaEngine(executionId, controlProvider, shell);
        }
    }
}
