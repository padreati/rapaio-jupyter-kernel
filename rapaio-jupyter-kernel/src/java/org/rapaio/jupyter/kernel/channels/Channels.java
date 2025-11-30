package org.rapaio.jupyter.kernel.channels;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.HMACDigest;
import org.rapaio.jupyter.kernel.message.Message;
import org.rapaio.jupyter.kernel.message.MessageId;
import org.rapaio.jupyter.kernel.message.MessageType;
import org.rapaio.jupyter.kernel.message.messages.ErrorReply;
import org.rapaio.jupyter.kernel.message.messages.IOPubStatus;
import org.rapaio.jupyter.kernel.message.messages.IOPubStream;
import org.zeromq.ZMQ;

@SuppressWarnings( {"unchecked", "rawtypes"})
public final class Channels {

    private final ConnectionProperties connProps;

    private final ZMQ.Context ctx;

    private final HeartbeatChannel heartbeat;
    private final ShellChannel shell;
    private final ControlChannel control;
    private final StdinChannel stdin;
    private final IOPubChannel iopub;

    private final Set<AbstractChannel> channels;
    private final Stack<Runnable> afterActions = new Stack<>();

    private Map<MessageType<?>, MessageHandler> handlers;
    private MessageId<?> msgId;

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
            iopub.sendMessage(new Message<>(MessageType.IOPUB_STATUS, IOPubStatus.STARTING));
            kernel.registerChannels(this);

            handlers = new HashMap<>();
            handlers.put(MessageType.SHELL_EXECUTE_REQUEST, kernel::handleExecuteRequest);
            handlers.put(MessageType.SHELL_INSPECT_REQUEST, kernel::handleInspectRequest);
            handlers.put(MessageType.SHELL_COMPLETE_REQUEST, kernel::handleCompleteRequest);
            handlers.put(MessageType.SHELL_HISTORY_REQUEST, kernel::handleHistoryRequest);
            handlers.put(MessageType.SHELL_IS_COMPLETE_REQUEST, kernel::handleIsCompleteRequest);
            handlers.put(MessageType.SHELL_KERNEL_INFO_REQUEST, kernel::handleKernelInfoRequest);
            handlers.put(MessageType.CONTROL_SHUTDOWN_REQUEST, kernel::handleShutdownRequest);
            handlers.put(MessageType.CONTROL_INTERRUPT_REQUEST, kernel::handleInterruptRequest);
            handlers.put(MessageType.CUSTOM_COMM_OPEN, kernel::handleCommOpenCommand);
            handlers.put(MessageType.CUSTOM_COMM_MSG, kernel::handleCommMsgCommand);
            handlers.put(MessageType.CUSTOM_COMM_CLOSE, kernel::handleCommCloseCommand);
            handlers.put(MessageType.SHELL_COMM_INFO_REQUEST, kernel::handleCommInfoRequest);

            isConnected = true;
        }
    }

    public <T> MessageHandler<T> getHandler(MessageType<T> type) {
        return handlers.get(type);
    }

    public void close() {
        channels.forEach(AbstractChannel::close);
        ctx.close();
    }

    public void joinUntilClose() {
        channels.forEach(AbstractChannel::joinUntilClose);
    }

    public boolean hasMsgId() {
        return msgId != null;
    }

    public void setMsgId(MessageId<?> msgId) {
        this.msgId = msgId;
    }

    public void freeMsgId() {
        this.msgId = null;
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
        return stdin.getInput(msgId, "", false);
    }

    public <T extends ContentType<T>> void publish(T content) {
        iopub.sendMessage(new Message<>(msgId, content.type(), content));
    }

    public <T extends ContentType<T>> void reply(T content) {
        shell.sendMessage(new Message<>(msgId, content.type(), content));
    }

    public void replyError(MessageType<?> type, ErrorReply error) {
        shell.sendMessage(new Message(msgId, type, error));
    }

    public void scheduleAfter(Runnable action) {
        afterActions.push(action);
    }

    public void runAfterActions() {
        while (!afterActions.isEmpty()) {
            afterActions.pop().run();
        }
    }

    public void busyThenIdle() {
        publish(IOPubStatus.BUSY);
        scheduleAfter(() -> publish(IOPubStatus.IDLE));
    }
}
