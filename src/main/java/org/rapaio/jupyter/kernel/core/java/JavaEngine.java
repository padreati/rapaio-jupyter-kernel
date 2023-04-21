package org.rapaio.jupyter.kernel.core.java;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
     *
     * @param restart if a restart is planned
     */
    public void shutdown(boolean restart) {
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

            Object value = subKind.isExecutable() ? control.takeResult(key) : event.value();
            result = value;
        }

        for (SnippetEvent event : events) {
            if (event.causeSnippet() == null) {
                JShellException e = event.exception();
                if (e != null) {
                    if (e instanceof EvalException) {
                        switch (((EvalException) e).getExceptionClassName()) {
                            case RapaioExecutionControl.TIMEOUT_MARKER ->
                                    throw new EvaluationTimeoutException(control.getTimeout(), code.trim());
                            case RapaioExecutionControl.INTERRUPTED_MARKER ->
                                    throw new EvaluationInterruptedException(code.trim());
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
