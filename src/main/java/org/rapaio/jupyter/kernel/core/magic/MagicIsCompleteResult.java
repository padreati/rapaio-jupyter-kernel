package org.rapaio.jupyter.kernel.core.magic;

import org.rapaio.jupyter.kernel.message.messages.ShellIsCompleteReply;

import java.util.Objects;

public record MagicIsCompleteResult(boolean handled, ShellIsCompleteReply.Status status, String indent) {

    public static MagicIsCompleteResult notHandled() {
        return new MagicIsCompleteResult(false, ShellIsCompleteReply.Status.UNKNOWN, ShellIsCompleteReply.NO_INDENT);
    }

    public MagicIsCompleteResult {
        if (handled && status != ShellIsCompleteReply.Status.INCOMPLETE && !Objects.equals(indent, ShellIsCompleteReply.NO_INDENT)) {
            throw new IllegalArgumentException("Only handled incomplete results can have indent.");
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
