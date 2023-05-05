package org.rapaio.jupyter.kernel.channels;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.core.Transform;
import org.rapaio.jupyter.kernel.message.HMACDigest;
import org.rapaio.jupyter.kernel.message.Header;
import org.rapaio.jupyter.kernel.message.Message;
import org.rapaio.jupyter.kernel.message.MessageType;
import org.rapaio.jupyter.kernel.message.messages.ErrorReply;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;


public abstract class AbstractChannel extends Thread {

    private static final Logger LOGGER = Logger.getLogger(AbstractChannel.class.getSimpleName());

    protected final ZMQ.Context ctx;
    protected final ZMQ.Socket socket;

    protected final HMACDigest hmacGenerator;
    protected final String channelName;
    protected final String logPrefix;
    protected boolean closed;

    protected AbstractChannel(String channelName, ZMQ.Context ctx, SocketType type, HMACDigest hmacGenerator) {
        this.ctx = ctx;
        this. socket = ctx.socket(type);
        this.channelName = channelName;
        this.logPrefix = "[" + channelName + "]: ";
        this.hmacGenerator = hmacGenerator;
        this.closed = false;
    }

    public abstract void bind(ConnectionProperties connProps);

    public synchronized Message<?> readMessage() {
        if (closed) {
            return null;
        }

        List<byte[]> identities = new LinkedList<>();
        while (true) {
            byte[] raw = socket.recv();
            if (Transform.equalsIdentityDelimiter(raw)) {
                break;
            }
            identities.add(raw);
        }

        String signature = socket.recvStr();

        byte[] headerRaw = socket.recv();
        byte[] parentHeaderRaw = socket.recv();
        byte[] metadataRaw = socket.recv();
        byte[] contentRaw = socket.recv();

        List<byte[]> blobs = new LinkedList<>();
        while (socket.hasReceiveMore()) {
            blobs.add(socket.recv());
        }

        String calculatedSig = this.hmacGenerator.calculateSignature(headerRaw, parentHeaderRaw, metadataRaw, contentRaw);

        if (calculatedSig != null && !calculatedSig.equals(signature)) {
            throw new SecurityException("Message received had invalid signature");
        }

        Header<?> header = Transform.fromJson(headerRaw, Header.class);
        Header<?> parentHeader = Transform.fromJsonNull(parentHeaderRaw, Header.class);

        Map<String, Object> metadata = Transform.fromJsonMap(metadataRaw);

        Object content = Transform.fromJson(contentRaw, header.type().getContentType());
        if (content instanceof ErrorReply) {
            header = new Header<>(header.id(), header.username(), header.sessionId(), header.timestamp(),
                    header.type().error(), header.version());
        }

        @SuppressWarnings("unchecked")
        Message<?> message = new Message(identities, header, parentHeader, metadata, content, blobs);

        LOGGER.finer("Received from " + socket.base().getSocketOptx(zmq.ZMQ.ZMQ_LAST_ENDPOINT) + ":\n" + Transform.toJson(message));

        return message;
    }

    @SuppressWarnings("unchecked")
    public <T> Message<T> readMessage(MessageType<T> type) {
        Message<?> message = readMessage();
        if (message.header().type() != type) {
            throw new RuntimeException("Expected a " + type + " message but received a " + message.header().type() + " message.");
        }
        return (Message<T>) message;
    }

    public synchronized void sendMessage(Message<?> message) {
        if (closed) {
            return;
        }

        byte[] headerRaw = Transform.toJsonBytes(message.header());
        byte[] parentHeaderRaw = Transform.toJsonBytes(message.parentHeader());
        byte[] metadata = Transform.toJsonBytes(message.metadata());
        byte[] content = Transform.toJsonBytes(message.content());

        String hmac = hmacGenerator.calculateSignature(headerRaw, parentHeaderRaw, metadata, content);

        LOGGER.finer("Sending to " + socket.base().getSocketOptx(zmq.ZMQ.ZMQ_LAST_ENDPOINT) + ":\n" + Transform.toJson(message));

        List<byte[]> chunks = new ArrayList<>(message.identities());
        chunks.add(Transform.IDENTITY_DELIMITER);
        chunks.add(hmac.getBytes(StandardCharsets.US_ASCII));
        chunks.add(headerRaw);
        chunks.add(parentHeaderRaw);
        chunks.add(metadata);
        chunks.add(content);
        if (message.blobs() != null) {
            chunks.addAll(message.blobs());
        }

        for (int i = 0; i < chunks.size(); i++) {
            if (i < chunks.size() - 1) {
                socket.sendMore(chunks.get(i));
            } else {
                socket.send(chunks.get(i));
            }
        }
    }

    public void close() {
        socket.close();
        closed = true;
    }

    public void joinUntilClose() {}
}
