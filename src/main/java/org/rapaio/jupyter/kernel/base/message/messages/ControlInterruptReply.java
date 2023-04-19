package org.rapaio.jupyter.kernel.base.message.messages;

import org.rapaio.jupyter.kernel.base.message.ContentType;
import org.rapaio.jupyter.kernel.base.message.MessageType;

public record ControlInterruptReply() implements ContentType<ControlInterruptReply> {

    @Override
    public MessageType<ControlInterruptReply> type() {
        return MessageType.CONTROL_INTERRUPT_REPLY;
    }
}
