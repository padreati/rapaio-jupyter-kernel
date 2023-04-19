package org.rapaio.jupyter.kernel.message.messages;

import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

import com.google.gson.annotations.SerializedName;

public record IOPubClearOutput(
        @SerializedName("wait") boolean shouldWait) implements ContentType<IOPubClearOutput> {

    @Override
    public MessageType<IOPubClearOutput> type() {
        return MessageType.IOPUB_CLEAR_OUTPUT;
    }
}
