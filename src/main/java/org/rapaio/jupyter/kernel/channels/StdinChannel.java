package org.rapaio.jupyter.kernel.channels;

import java.util.logging.Logger;

import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.message.HMACDigest;
import org.rapaio.jupyter.kernel.message.MessageContext;
import org.rapaio.jupyter.kernel.message.Message;
import org.rapaio.jupyter.kernel.message.MessageType;
import org.rapaio.jupyter.kernel.message.messages.StdinInputReply;
import org.rapaio.jupyter.kernel.message.messages.StdinInputRequest;
import org.rapaio.jupyter.kernel.util.Formatter;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class StdinChannel extends AbstractChannel {

    private static final Logger LOGGER = Logger.getLogger("StdinChannel");

    public StdinChannel(ZMQ.Context context, HMACDigest hmacGenerator) {
        super("StdinChannel", context, SocketType.ROUTER, hmacGenerator);
    }

    @Override
    public void bind(ConnectionProperties connProps) {
        String addr = Formatter.formatAddress(connProps.transport(), connProps.ip(), connProps.stdinPort());

        LOGGER.info(logPrefix + String.format("Binding stdin to %s.", addr));
        socket.bind(addr);
    }

    /**
     * Ask the frontend for input.
     * <p>
     * <strong>Do not ask for input if an execute request has `allow_stdin=False`</strong>
     *
     * @param context           a message that the request with input was invoked by such as an execute request
     * @param prompt            a prompt string for the front end to include with the input request
     * @param isPasswordRequest a flag specifying if the input request is for a password, if so
     *                          the frontend should obscure the user input (for example with password
     *                          dots or not echoing the input)
     * @return the input string from the frontend.
     */
    public synchronized String getInput(MessageContext<?> context, String prompt, boolean isPasswordRequest) {
        StdinInputRequest content = new StdinInputRequest(prompt, isPasswordRequest);
        Message<StdinInputRequest> request = new Message<>(context, MessageType.STDIN_INPUT_REQUEST, null, content, null);
        sendMessage(request);

        Message<StdinInputReply> reply = readMessage(MessageType.STDIN_INPUT_REPLY);
        return reply.content().value() + System.lineSeparator();
    }
}
