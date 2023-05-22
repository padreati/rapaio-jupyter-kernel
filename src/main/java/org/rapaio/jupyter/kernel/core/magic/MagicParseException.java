package org.rapaio.jupyter.kernel.core.magic;

import java.util.stream.Collectors;

public class MagicParseException extends Exception {

    private final String parser;
    private final String snippet;

    public MagicParseException(String parser, MagicSnippet magicSnippet, String message) {
        super(message);
        this.parser = parser;
        this.snippet = magicSnippet.lines().stream().map(MagicSnippet.CodeLine::code).collect(Collectors.joining("\n"));
    }

    public MagicParseException(String parser, String snippet, String message) {
        super(message);
        this.parser = parser;
        this.snippet = snippet;
    }

    public String getParser() {
        return parser;
    }

    public String getMagicSnippet() {
        return snippet;
    }
}
