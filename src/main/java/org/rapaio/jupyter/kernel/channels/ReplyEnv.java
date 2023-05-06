package org.rapaio.jupyter.kernel.channels;

import java.util.Stack;

import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.Message;
import org.rapaio.jupyter.kernel.message.MessageContext;
import org.rapaio.jupyter.kernel.message.MessageType;
import org.rapaio.jupyter.kernel.message.messages.ErrorReply;
import org.rapaio.jupyter.kernel.message.messages.IOPubStatus;
import org.rapaio.jupyter.kernel.message.messages.IOPubStream;

@SuppressWarnings("rawtypes")
public final class ReplyEnv {

    private final MessageContext context;
    private final JupyterChannels channels;

    private final Stack<Runnable> delayedActions = new Stack<>();

    private boolean requestShutdown = false;

    ReplyEnv(JupyterChannels channels, MessageContext context) {
        this.channels = channels;
        this.context = context;
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
        return channels.stdin().getInput(context, "", false);
    }

    public <T extends ContentType<T>> void publish(T content) {
        channels.iopub().sendMessage(new Message<>(context, content.type(), null, content, null));
    }

    public <T extends ContentType<T>> void reply(T content) {
        channels.shell().sendMessage(new Message<>(context, content.type(), null, content, null));
    }

    @SuppressWarnings("unchecked")
    public void replyError(MessageType<?> type, ErrorReply error) {
        channels.shell().sendMessage(new Message(context, type, null, error, null));
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
