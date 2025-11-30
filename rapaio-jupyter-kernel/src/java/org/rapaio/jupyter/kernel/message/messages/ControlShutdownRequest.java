package org.rapaio.jupyter.kernel.message.messages;

import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

public record ControlShutdownRequest(boolean restart) implements ContentType<ControlShutdownRequest> {

    @Override
    public MessageType<ControlShutdownRequest> type() {
        return MessageType.CONTROL_SHUTDOWN_REQUEST;
    }
}
