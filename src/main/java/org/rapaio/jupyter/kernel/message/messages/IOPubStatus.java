package org.rapaio.jupyter.kernel.message.messages;

import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

import com.google.gson.annotations.SerializedName;

public record IOPubStatus(
        @SerializedName("execution_state") State state) implements ContentType<IOPubStatus> {

    @Override
    public MessageType<IOPubStatus> type() {
        return MessageType.IOPUB_STATUS;
    }

    public static final IOPubStatus BUSY = new IOPubStatus(State.BUSY);
    public static final IOPubStatus IDLE = new IOPubStatus(State.IDLE);
    public static final IOPubStatus STARTING = new IOPubStatus(State.STARTING);

    public enum State {
        @SerializedName("busy") BUSY,
        @SerializedName("idle") IDLE,
        @SerializedName("starting") STARTING
    }
}
