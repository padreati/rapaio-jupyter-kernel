package org.rapaio.jupyter.kernel.core.magic;

public class MagicEvalException extends Exception {

    private final MagicSnippet magicSnippet;
    private final int errorLine;
    private final int errorStart;
    private final int errorEnd;

    public MagicEvalException(MagicSnippet magicSnippet, String errorMessage) {
        super(errorMessage);
        this.magicSnippet = magicSnippet;
        this.errorLine = -1;
        this.errorStart = 0;
        this.errorEnd = 0;
    }

    public MagicEvalException(MagicSnippet magicSnippet, String errorMessage, int errorLine, int errorStart, int errorEnd) {
        super(errorMessage);
        this.magicSnippet = magicSnippet;
        this.errorLine = errorLine;
        this.errorStart = errorStart;
        this.errorEnd = errorEnd;
    }

    public MagicSnippet magicSnippet() {
        return magicSnippet;
    }

    public boolean hasErrorLine() {
        return errorLine >= 0;
    }

    public int errorLine() {
        return errorLine;
    }

    public int errorStart() {
        return errorStart;
    }

    public int errorEnd() {
        return errorEnd;
    }
}
