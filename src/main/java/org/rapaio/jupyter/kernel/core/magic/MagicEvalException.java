package org.rapaio.jupyter.kernel.core.magic;

import jdk.jshell.Snippet;

public class MagicEvalException extends Exception {

    private final MagicSnippet magicSnippet;

    public MagicEvalException(MagicSnippet magicSnippet, String errorMessage) {
        super(errorMessage);
        this.magicSnippet = magicSnippet;
    }

    public MagicSnippet magicSnippet() {
        return magicSnippet;
    }
}
