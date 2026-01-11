package org.rapaio.jupyter.kernel.message.messages;

import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

public record StdinInputRequest(String prompt, boolean password) implements ContentType<StdinInputRequest> {

    @Override
    public MessageType<StdinInputRequest> type() {
        return MessageType.STDIN_INPUT_REQUEST;
    }
}
