package org.rapaio.jupyter.kernel.message.messages;

import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

import com.google.gson.annotations.SerializedName;

public record ShellCompleteRequest(
        @SerializedName("code") String code,
        @SerializedName("cursor_pos") int cursorPos) implements ContentType<ShellCompleteRequest> {

    @Override
    public MessageType<ShellCompleteRequest> type() {
        return MessageType.SHELL_COMPLETE_REQUEST;
    }
}
