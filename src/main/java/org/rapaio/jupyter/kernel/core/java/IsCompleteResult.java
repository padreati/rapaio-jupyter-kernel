package org.rapaio.jupyter.kernel.core.java;

import org.rapaio.jupyter.kernel.message.messages.ShellIsCompleteReply;

public record IsCompleteResult(ShellIsCompleteReply.Status status, String indent) {

    public IsCompleteResult(ShellIsCompleteReply.Status status) {
        this(status, ShellIsCompleteReply.NO_INDENT);
    }

    public IsCompleteResult {
        if (status != ShellIsCompleteReply.Status.INCOMPLETE && !ShellIsCompleteReply.NO_INDENT.equals(indent)) {
            throw new IllegalArgumentException("Only incomplete results can have an indent.");
        }
    }

    public ShellIsCompleteReply buildReply() {
        return switch (status) {
            case COMPLETE -> ShellIsCompleteReply.complete();
            case INVALID -> ShellIsCompleteReply.invalid();
            case UNKNOWN -> ShellIsCompleteReply.unknown();
            case INCOMPLETE -> ShellIsCompleteReply.incomplete(indent);
        };
    }
}
