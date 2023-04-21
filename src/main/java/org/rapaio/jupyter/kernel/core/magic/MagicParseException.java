package org.rapaio.jupyter.kernel.core.magic;

public class MagicParseException extends Exception {

    private final String parser;
    private final String snippet;

    public MagicParseException(String parser, String snippet, String message) {
        super(message);
        this.parser = parser;
        this.snippet = snippet;
    }

    public String getParser() {
        return parser;
    }

    public String getSnippet() {
        return snippet;
    }
}
