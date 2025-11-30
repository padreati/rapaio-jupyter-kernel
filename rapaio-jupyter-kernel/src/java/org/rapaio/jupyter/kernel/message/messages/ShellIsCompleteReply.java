package org.rapaio.jupyter.kernel.message.messages;

import com.google.gson.annotations.SerializedName;
import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

public record ShellIsCompleteReply(Status status, String indent) implements ContentType<ShellIsCompleteReply> {

    public static final String NO_INDENT = "";

    @Override
    public MessageType<ShellIsCompleteReply> type() {
        return MessageType.SHELL_IS_COMPLETE_REPLY;
    }

    public static ShellIsCompleteReply complete() {
        return new ShellIsCompleteReply(Status.COMPLETE, NO_INDENT);
    }

    public static ShellIsCompleteReply invalid() {
        return new ShellIsCompleteReply(Status.INVALID, NO_INDENT);
    }

    public static ShellIsCompleteReply unknown() {
        return new ShellIsCompleteReply(Status.UNKNOWN, NO_INDENT);
    }

    public static ShellIsCompleteReply incomplete(String indent) {
        return new ShellIsCompleteReply(Status.INCOMPLETE, indent);
    }

    public enum Status {
        @SerializedName("complete") COMPLETE,
        @SerializedName("incomplete") INCOMPLETE,
        @SerializedName("invalid") INVALID,
        @SerializedName("unknown") UNKNOWN
    }
}
