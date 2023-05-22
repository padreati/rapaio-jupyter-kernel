package org.rapaio.jupyter.kernel.channels;

import java.util.logging.Logger;

import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.message.HMACDigest;
import org.rapaio.jupyter.kernel.message.MessageId;
import org.rapaio.jupyter.kernel.message.Message;
import org.rapaio.jupyter.kernel.message.MessageType;
import org.rapaio.jupyter.kernel.message.messages.StdinInputReply;
import org.rapaio.jupyter.kernel.message.messages.StdinInputRequest;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public final class StdinChannel extends AbstractChannel {

    private static final Logger LOGGER = Logger.getLogger(StdinChannel.class.getSimpleName());

    public StdinChannel(Channels channels, ZMQ.Context context, HMACDigest hmacGenerator) {
        super(channels, "StdinChannel", context, SocketType.ROUTER, hmacGenerator);
    }

    @Override
    public void bind(ConnectionProperties connProps) {
        String addr = connProps.formatAddress(connProps.stdinPort());

        LOGGER.info(logPrefix + "Binding stdin to " + addr);
        socket.bind(addr);
    }

    public synchronized String getInput(MessageId<?> messageId, String prompt, boolean isPasswordRequest) {
        StdinInputRequest content = new StdinInputRequest(prompt, isPasswordRequest);
        Message<StdinInputRequest> request = new Message<>(messageId, MessageType.STDIN_INPUT_REQUEST, content);
        sendMessage(request);

        Message<StdinInputReply> reply = readMessage(MessageType.STDIN_INPUT_REPLY);
        return reply.content().value() + System.lineSeparator();
    }
}
