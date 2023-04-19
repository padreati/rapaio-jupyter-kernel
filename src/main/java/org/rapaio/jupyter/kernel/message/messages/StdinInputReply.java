package org.rapaio.jupyter.kernel.message.messages;

import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

public record StdinInputReply(String value) implements ContentType<StdinInputReply> {

    @Override
    public MessageType<StdinInputReply> type() {
        return MessageType.STDIN_INPUT_REPLY;
    }
}
