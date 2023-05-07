package org.rapaio.jupyter.kernel.channels;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.message.HMACDigest;
import org.rapaio.jupyter.kernel.message.Message;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public final class ShellChannel extends AbstractChannel {

    private static final Logger LOGGER = Logger.getLogger(ShellChannel.class.getSimpleName());
    private static final long LOOP_SLEEP = 50;
    private static final AtomicInteger ID = new AtomicInteger();

    private final JupyterChannels connection;
    private volatile LoopThread loopThread;

    public ShellChannel(ZMQ.Context context, HMACDigest hmacGenerator, JupyterChannels connection) {
        super("ShellChannel", context, SocketType.ROUTER, hmacGenerator);
        this.connection = connection;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void bind(ConnectionProperties connProps) {
        if (loopThread != null) {
            throw new IllegalStateException("Channel already bound.");
        }

        String channelThreadName = "Shell-" + ID.getAndIncrement();
        String addr = connProps.formatAddress(connProps.shellPort());

        LOGGER.info(logPrefix + String.format("Binding %s to %s.", channelThreadName, addr));
        socket.bind(addr);

        ZMQ.Poller poller = ctx.poller(1);
        poller.register(socket, ZMQ.Poller.POLLIN);

        this.loopThread = new LoopThread(channelThreadName, LOOP_SLEEP, () -> {
            int events = poller.poll(0);
            if (events > 0) {
                Message<?> message = readMessage();
                var type = message.header().type();
                MessageHandler handler = connection.getHandler(type);
                if (handler != null) {
                    LOGGER.info(logPrefix + "Handling message: " + type.getName());
                    ReplyEnv env = connection.newReplyEnv(message.getContext());
                    try {
                        handler.handle(env, message);
                    } catch (Exception e) {
                        LOGGER.severe(logPrefix + "Exception handling " + type.getName() + ". " +
                                e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
                    } finally {
                        env.runDelayedActions();
                    }
                    if (env.isMarkedForShutdown()) {
                        LOGGER.info(logPrefix + "Shutting down connection marked for shutdown.");
                        connection.close();
                    }
                } else {
                    LOGGER.severe(logPrefix + "Unhandled message: " + type.getName());
                }
            }
        });
        loopThread.start();
    }

    @Override
    public void close() {
        if (loopThread != null) {
            loopThread.shutdown();
        }
        super.close();
    }

    @Override
    public void joinUntilClose() {
        if (loopThread != null) {
            try {
                loopThread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

}
