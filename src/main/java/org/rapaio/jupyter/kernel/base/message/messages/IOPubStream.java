package org.rapaio.jupyter.kernel.base.message.messages;

import org.rapaio.jupyter.kernel.base.message.ContentType;
import org.rapaio.jupyter.kernel.base.message.MessageType;

import com.google.gson.annotations.SerializedName;

public record IOPubStream(
        StreamName name,
        String text) implements ContentType<IOPubStream> {

    @Override
    public MessageType<IOPubStream> type() {
        return MessageType.IOPUB_STREAM;
    }

    public enum StreamName {
        @SerializedName("stdout") OUT,
        @SerializedName("stderr") ERR
    }
}
