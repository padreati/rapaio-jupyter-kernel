package org.rapaio.jupyter.kernel.message.messages;

import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

import com.google.gson.annotations.SerializedName;

public record ShellIsCompleteReply(
        Status status,
        /*
         * If status is INVALID_CODE this is a hint for the front end on what
         * to use for the indent on the next line.
         */
        String indent
) implements ContentType<ShellIsCompleteReply> {

    @Override
    public MessageType<ShellIsCompleteReply> type() {
        return MessageType.SHELL_IS_COMPLETE_REPLY;
    }

    public static final ShellIsCompleteReply VALID_CODE = new ShellIsCompleteReply(Status.VALID_CODE, "");
    public static final ShellIsCompleteReply INVALID_CODE = new ShellIsCompleteReply(Status.INVALID_CODE, "");
    public static final ShellIsCompleteReply UNKNOWN = new ShellIsCompleteReply(Status.UNKNOWN, "");

    private static final ShellIsCompleteReply[] COMMON_INDENTS = {
            new ShellIsCompleteReply(Status.NOT_FINISHED, ""),
            new ShellIsCompleteReply(Status.NOT_FINISHED, " "),
            new ShellIsCompleteReply(Status.NOT_FINISHED, "  "),
            new ShellIsCompleteReply(Status.NOT_FINISHED, "   "),
            new ShellIsCompleteReply(Status.NOT_FINISHED, "    "),
            new ShellIsCompleteReply(Status.NOT_FINISHED, "     "),
            new ShellIsCompleteReply(Status.NOT_FINISHED, "      "),
            new ShellIsCompleteReply(Status.NOT_FINISHED, "       "),
            new ShellIsCompleteReply(Status.NOT_FINISHED, "        "),
            new ShellIsCompleteReply(Status.NOT_FINISHED, "\t"),
            new ShellIsCompleteReply(Status.NOT_FINISHED, "\t\t")
    };

    /**
     * Try to resolve the indent to a common, shared instance, otherwise
     * create a new one. Since many indent replies will be a short sequence
     * or whitespace or an empty string we can cache some of these.
     *
     * @param indent the indent to suggest the frontend prefixes the next
     *               line with
     * @return a reply describing the indent suggestion
     */
    public static ShellIsCompleteReply getIncompleteReplyWithIndent(String indent) {
        for (var reply : COMMON_INDENTS) {
            if (reply.indent.equals(indent)) {
                return reply;
            }
        }
        return new ShellIsCompleteReply(Status.NOT_FINISHED, indent);
    }

    public enum Status {
        @SerializedName("complete") VALID_CODE,
        @SerializedName("incomplete") NOT_FINISHED,
        @SerializedName("invalid") INVALID_CODE,
        @SerializedName("unknown") UNKNOWN
    }
}
