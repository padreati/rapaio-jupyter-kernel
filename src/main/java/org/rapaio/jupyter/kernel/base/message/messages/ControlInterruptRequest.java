package org.rapaio.jupyter.kernel.base.message.messages;

import org.rapaio.jupyter.kernel.base.message.ContentType;
import org.rapaio.jupyter.kernel.base.message.MessageType;

public record ControlInterruptRequest() implements ContentType<ControlInterruptRequest> {

    @Override
    public MessageType<ControlInterruptRequest> type() {
        return MessageType.CONTROL_INTERRUPT_REQUEST;
    }
}
