package org.rapaio.jupyter.kernel.channels;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.HMACDigest;
import org.rapaio.jupyter.kernel.message.Message;
import org.rapaio.jupyter.kernel.message.MessageContext;
import org.rapaio.jupyter.kernel.message.MessageType;
import org.rapaio.jupyter.kernel.message.messages.ErrorReply;
import org.rapaio.jupyter.kernel.message.messages.IOPubStatus;
import org.rapaio.jupyter.kernel.message.messages.IOPubStream;
import org.zeromq.ZMQ;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class Channels {

    private final ConnectionProperties connProps;

    private final ZMQ.Context ctx;

    private final HeartbeatChannel heartbeat;
    private final ShellChannel shell;
    private final ControlChannel control;
    private final StdinChannel stdin;
    private final IOPubChannel iopub;

    private final Set<AbstractChannel> channels;
    private final Stack<Runnable> delayedActions = new Stack<>();

    private Map<MessageType<?>, MessageHandler> messageHandlers;
    private MessageContext<?> context;

    private boolean isConnected = false;
    private boolean requestShutdown = false;

    public Channels(ConnectionProperties connProps) throws NoSuchAlgorithmException, InvalidKeyException {
        this.connProps = connProps;
        ctx = ZMQ.context(1);

        HMACDigest hmacDigest = connProps.createHMACDigest();

        heartbeat = new HeartbeatChannel(this, ctx, hmacDigest);
        shell = new ShellChannel(this, ctx, hmacDigest, this);
        control = new ControlChannel(this, ctx, hmacDigest);
        stdin = new StdinChannel(this, ctx, hmacDigest);
        iopub = new IOPubChannel(this, ctx, hmacDigest);

        channels = Set.of(heartbeat, shell, control, stdin, iopub);
    }

    public void connect(RapaioKernel kernel) {
        if (!isConnected) {
            channels.forEach(s -> s.bind(connProps));
            iopub.sendMessage(new Message<>(null, MessageType.IOPUB_STATUS, null, IOPubStatus.STARTING, null));
            messageHandlers = kernel.registerChannels(this);
            isConnected = true;
        }
    }

    public <T> MessageHandler<T> getHandler(MessageType<T> type) {
        return messageHandlers.get(type);
    }

    public void close() {
        channels.forEach(AbstractChannel::close);
        ctx.close();
    }

    public void joinUntilClose() {
        channels.forEach(AbstractChannel::joinUntilClose);
    }

    public boolean hasContext() {
        return context != null;
    }

    public void setContext(MessageContext<?> context) {
        this.context = context;
    }

    public void freeContext() {
        this.context = null;
    }

    public void markForShutdown() {
        requestShutdown = true;
    }

    public boolean isMarkedForShutdown() {
        return requestShutdown;
    }

    public void writeToStdOut(String msg) {
        publish(new IOPubStream(IOPubStream.StreamName.OUT, msg));
    }

    public void writeToStdErr(String msg) {
        publish(new IOPubStream(IOPubStream.StreamName.ERR, msg));
    }

    public String readFromStdIn() {
        return stdin.getInput(context, "", false);
    }

    public <T extends ContentType<T>> void publish(T content) {
        iopub.sendMessage(new Message<>(context, content.type(), null, content, null));
    }

    public <T extends ContentType<T>> void reply(T content) {
        shell.sendMessage(new Message<>(context, content.type(), null, content, null));
    }

    public void replyError(MessageType<?> type, ErrorReply error) {
        shell.sendMessage(new Message(context, type, null, error, null));
    }

    public void delay(Runnable action) {
        delayedActions.push(action);
    }

    public void runDelayedActions() {
        while (!delayedActions.isEmpty()) {
            delayedActions.pop().run();
        }
    }

    public void busyThenIdle() {
        publish(IOPubStatus.BUSY);
        delay(() -> publish(IOPubStatus.IDLE));
    }
}
