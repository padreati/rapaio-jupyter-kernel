package org.rapaio.jupyter.kernel.channels;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.message.HMACDigest;
import org.rapaio.jupyter.kernel.message.Message;
import org.rapaio.jupyter.kernel.message.MessageContext;
import org.rapaio.jupyter.kernel.message.MessageType;
import org.rapaio.jupyter.kernel.message.messages.IOPubStatus;
import org.zeromq.ZMQ;

public final class JupyterChannels {

    private final ConnectionProperties connProps;

    private final ZMQ.Context ctx;

    private final HeartbeatChannel heartbeat;
    private final ShellChannel shell;
    private final ControlChannel control;
    private final StdinChannel stdin;
    private final IOPubChannel iopub;

    private final Set<AbstractChannel> channels;

    private final RapaioKernel kernel;

    private boolean isConnected = false;

    public JupyterChannels(ConnectionProperties connProps, RapaioKernel kernel) throws NoSuchAlgorithmException, InvalidKeyException {
        this.connProps = connProps;
        this.kernel = kernel;
        ctx = ZMQ.context(1);

        HMACDigest hmacDigest = connProps.createHMACDigest();

        heartbeat = new HeartbeatChannel(this.ctx, hmacDigest);
        shell = new ShellChannel(this.ctx, hmacDigest, this);
        control = new ControlChannel(this.ctx, hmacDigest, this);
        stdin = new StdinChannel(this.ctx, hmacDigest);
        iopub = new IOPubChannel(this.ctx, hmacDigest);

        channels = Set.of(heartbeat, shell, control, stdin, iopub);
    }

    public void connect() {
        if (!isConnected) {
            channels.forEach(s -> s.bind(this.connProps));
            iopub.sendMessage(new Message<>(null, MessageType.IOPUB_STATUS, null, IOPubStatus.STARTING, null));
            kernel.registerChannels(this);
            isConnected = true;
        }
    }

    public ShellChannel shell() {
        return shell;
    }

    public ControlChannel control() {
        return control;
    }

    public IOPubChannel iopub() {
        return iopub;
    }

    public StdinChannel stdin() {
        return stdin;
    }

    public HeartbeatChannel heartbeat() {
        return heartbeat;
    }

    @SuppressWarnings("unchecked")
    public <T> MessageHandler<T> getHandler(MessageType<T> type) {
        return kernel.getHandler(type);
    }

    public ReplyEnv prepareReplyEnv(MessageContext<?> context) {
        return new ReplyEnv(this, context);
    }

    public void close() {
        channels.forEach(AbstractChannel::close);
        ctx.close();
    }

    public void joinUntilClose() {
        channels.forEach(AbstractChannel::joinUntilClose);
    }
}
