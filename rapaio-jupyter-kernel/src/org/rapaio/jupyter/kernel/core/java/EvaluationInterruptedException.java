package org.rapaio.jupyter.kernel.core.java;

public class EvaluationInterruptedException extends Exception {
    private final String source;

    public EvaluationInterruptedException(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String getMessage() {
        return "Evaluator was interrupted while executing: " + source;
    }
}
