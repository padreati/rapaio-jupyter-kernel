package org.rapaio.jupyter.kernel.channels;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.message.HMACDigest;
import org.rapaio.jupyter.kernel.message.Message;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class ControlChannel extends AbstractChannel {

    private static final Logger LOGGER = Logger.getLogger(ControlChannel.class.getSimpleName());
    private static final long SHELL_DEFAULT_LOOP_SLEEP_MS = 50;

    private static final AtomicInteger ID = new AtomicInteger();

    protected final JupyterChannels connection;
    protected volatile LoopThread loopThread;

    public ControlChannel(ZMQ.Context context, HMACDigest hmacGenerator, JupyterChannels connection) {
        super("ControlChannel", context, SocketType.ROUTER, hmacGenerator);
        this.connection = connection;
    }

    @Override
    @SuppressWarnings( {"rawtypes", "unchecked"})
    public void bind(ConnectionProperties connProps) {
        if (isBound()) {
            throw new IllegalStateException("Shell channel already bound");
        }

        String channelThreadName = "Shell-" + ID.getAndIncrement();
        String addr = formatAddress(connProps.transport(), connProps.ip(), connProps.controlPort());

        LOGGER.info(logPrefix + String.format("Binding %s to %s.", channelThreadName, addr));
        socket.bind(addr);

        ZMQ.Poller poller = ctx.poller(1);
        poller.register(socket, ZMQ.Poller.POLLIN);

        this.loopThread = new LoopThread(channelThreadName, SHELL_DEFAULT_LOOP_SLEEP_MS, () -> {
            if (poller.poll(0) > 0) {
                Message<?> message = readMessage();
                MessageHandler handler = connection.getHandler(message.header().type());
                if (handler != null) {
                    LOGGER.info(logPrefix + "Handling message: " + message.header().type().getName());
                    ReplyEnv env = connection.prepareReplyEnv(message.getContext());
                    try {
                        handler.handle(env, message);
                    } catch (Exception e) {
                        LOGGER.severe(logPrefix + "Unhandled exception handling " + message.header().type().getName() + ". " + e.getClass()
                                .getSimpleName() + " - " + e.getLocalizedMessage());
                    } finally {
                        env.runDelayedActions();
                    }
                    if (env.isMarkedForShutdown()) {
                        LOGGER.info(logPrefix + channelThreadName + " shutting down connection as environment was marked for shutdown.");
                        connection.close();
                    }
                } else {
                    LOGGER.severe(logPrefix + "Unhandled message: " + message.header().type().getName());
                }
            }
        });
        loopThread.start();
    }

    protected boolean isBound() {
        return loopThread != null;
    }

    @Override
    public void close() {
        if (isBound()) {
            loopThread.shutdown();
        }
        super.close();
    }

    @Override
    public void joinUntilClose() {
        if (this.loopThread != null) {
            try {
                this.loopThread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
