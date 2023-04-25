package org.rapaio.jupyter.kernel.channels;

import java.util.Deque;
import java.util.LinkedList;

import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.Message;
import org.rapaio.jupyter.kernel.message.MessageContext;
import org.rapaio.jupyter.kernel.message.MessageType;
import org.rapaio.jupyter.kernel.message.messages.ErrorReply;
import org.rapaio.jupyter.kernel.message.messages.IOPubStatus;
import org.rapaio.jupyter.kernel.message.messages.IOPubStream;

@SuppressWarnings("rawtypes")
public final class ReplyEnv {

    private final IOPubChannel iopub;
    private final ShellChannel shell;
    private final ControlChannel control;
    private final StdinChannel stdin;
    private final HeartbeatChannel heartbeat;
    private final MessageContext context;

    private final Deque<Runnable> deferred = new LinkedList<>();

    private boolean requestShutdown = false;
    private boolean defer = false;

    ReplyEnv(JupyterChannels channels, MessageContext context) {
        this.iopub = channels.iopub();
        this.shell = channels.shell();
        this.control = channels.control();
        this.stdin = channels.stdin();
        this.heartbeat = channels.heartbeat();

        this.context = context;
    }

    public ReplyEnv defer() {
        this.defer = true;
        return this;
    }

    public void markForShutdown() {
        this.requestShutdown = true;
    }

    public boolean isMarkedForShutdown() {
        return this.requestShutdown;
    }

    public void writeToStdOut(String msg) {
        publish(new IOPubStream(IOPubStream.StreamName.OUT, msg));
    }

    public void writeToStdErr(String msg) {
        publish(new IOPubStream(IOPubStream.StreamName.ERR, msg));
    }

    public String readFromStdIn(String prompt, boolean isPassword) {
        return stdin.getInput(getContext(), prompt, isPassword);
    }

    public String readFromStdIn() {
        return this.readFromStdIn("", false);
    }

    public MessageContext<?> getContext() {
        return context;
    }

    public void publish(Message<?> msg) {
        if (defer) {
            deferred.push(() -> iopub.sendMessage(msg));
            this.defer = false;
        } else {
            iopub.sendMessage(msg);
        }
    }

    public void reply(Message<?> msg) {
        if (defer) {
            deferred.push(() -> shell.sendMessage(msg));
            this.defer = false;
        } else {
            shell.sendMessage(msg);
        }
    }

    /**
     * Defer an arbitrary action. See {@link #defer()} but instead of
     * deferring the next message send, defer a specific action.
     *
     * @param action the action to run when the deferrals are resolved
     */
    public void defer(Runnable action) {
        this.deferred.push(action);
    }

    public void resolveDeferrals() {
        if (this.defer) {
            throw new IllegalStateException("Reply environment is in defer mode but a resolution was request.");
        }

        while (!deferred.isEmpty()) {
            deferred.pop().run();
        }
    }

    public <T extends ContentType<T>> void publish(T content) {
        publish(new Message<>(context, content.type(), null, content, null));
    }

    public <T extends ContentType<T>> void reply(T content) {
        reply(new Message<>(context, content.type(), null, content, null));
    }

    @SuppressWarnings("unchecked")
    public void replyError(MessageType<?> type, ErrorReply error) {
        reply(new Message(context, type, null, error, null));
    }

    public void setBusyDeferIdle() {
        publish(IOPubStatus.BUSY);
        defer();
        publish(IOPubStatus.IDLE);
    }
}
