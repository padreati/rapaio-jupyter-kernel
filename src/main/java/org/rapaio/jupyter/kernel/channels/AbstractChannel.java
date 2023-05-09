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


public abstract sealed class AbstractChannel extends Thread
        permits ControlChannel, HeartbeatChannel, ShellChannel, IOPubChannel, StdinChannel {

    private static final Logger LOGGER = Logger.getLogger(AbstractChannel.class.getSimpleName());

    protected final ZMQ.Context ctx;
    protected final ZMQ.Socket socket;

    protected final HMACDigest hmacGenerator;
    protected final String channelName;
    protected final Channels channels;
    protected final String logPrefix;
    protected boolean closed;

    protected AbstractChannel(Channels channels, String channelName, ZMQ.Context ctx, SocketType type, HMACDigest hmacGenerator) {
        this.channels = channels;
        this.ctx = ctx;
        this.socket = ctx.socket(type);
        this.channelName = channelName;
        this.logPrefix = "[" + channelName + "]: ";
        this.hmacGenerator = hmacGenerator;
        this.closed = false;
    }

    public abstract void bind(ConnectionProperties connProps);

    @SuppressWarnings({"unchecked","rawtypes"})
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

        String computedSignature = hmacGenerator.computeSignature(headerRaw, parentHeaderRaw, metadataRaw, contentRaw);

        if (computedSignature != null && !computedSignature.equals(signature)) {
            throw new SecurityException("Invalid signature on message received.");
        }

        Header<?> header = Transform.fromJson(headerRaw, Header.class);
        Header<?> parentHeader = Transform.fromJsonNull(parentHeaderRaw, Header.class);
        Map<String, Object> metadata = Transform.fromJsonMap(metadataRaw);
        Object content = Transform.fromJson(contentRaw, header.type().contentType());
        if (content instanceof ErrorReply) {
            header = new Header<>(header.id(), header.username(), header.sessionId(), header.timestamp(),
                    header.type().newError(), header.version());
        }
        return new Message(identities, header, parentHeader, metadata, content, blobs);
    }

    @SuppressWarnings("unchecked")
    public <T> Message<T> readMessage(MessageType<T> type) {
        Message<?> message = readMessage();
        if (message.header().type() != type) {
            throw new RuntimeException("Expected message of type:" + type + ", but received one with type:" + message.header().type());
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

        String hmac = hmacGenerator.computeSignature(headerRaw, parentHeaderRaw, metadata, content);

        LOGGER.finest("Sending to " + socket.base().getSocketOptx(zmq.ZMQ.ZMQ_LAST_ENDPOINT) + ":\n"
                + Transform.toJson(message));

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

        for (int i = 0; i < chunks.size() - 1; i++) {
            socket.sendMore(chunks.get(i));
        }
        socket.send(chunks.get(chunks.size() - 1));
    }

    public void close() {
        socket.close();
        closed = true;
    }

    public void joinUntilClose() {
    }
}
