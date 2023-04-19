package org.rapaio.jupyter.kernel.message.messages;

import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

public record ControlShutdownReply(boolean restart) implements ContentType<ControlShutdownReply> {

    @Override
    public MessageType<ControlShutdownReply> type() {
        return MessageType.CONTROL_SHUTDOWN_REPLY;
    }

    public static final ControlShutdownReply SHUTDOWN_AND_RESTART = new ControlShutdownReply(true);
    public static final ControlShutdownReply SHUTDOWN = new ControlShutdownReply(false);

}
