package org.rapaio.jupyter.kernel.message.messages;

import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

public record ControlInterruptReply() implements ContentType<ControlInterruptReply> {

    @Override
    public MessageType<ControlInterruptReply> type() {
        return MessageType.CONTROL_INTERRUPT_REPLY;
    }
}
