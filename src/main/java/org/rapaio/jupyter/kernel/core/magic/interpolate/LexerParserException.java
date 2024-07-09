package org.rapaio.jupyter.kernel.core.magic.interpolate;

public class LexerParserException extends Exception {

    private final int position;
    private final int length;

    public LexerParserException(String message, int position, int length) {
        super(message);
        this.position = position;
        this.length = length;
    }

    public LexerParserException(String message, int position, int length, Throwable cause) {
        super(message, cause);
        this.position = position;
        this.length = length;
    }

    public int position() {
        return position;
    }

    public int length() {
        return length;
    }
}
