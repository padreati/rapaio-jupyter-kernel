package org.rapaio.jupyter.kernel.channels;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.message.HMACDigest;
import org.rapaio.jupyter.kernel.message.Message;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public final class ControlChannel extends AbstractChannel {

    private static final Logger LOGGER = Logger.getLogger(ControlChannel.class.getSimpleName());
    private static final long LOOP_SLEEP = 50;
    private static final AtomicInteger ID = new AtomicInteger();

    private final Channels channels;
    private volatile LoopThread loopThread;

    public ControlChannel(Channels channels, ZMQ.Context context, HMACDigest hmacGenerator) {
        super(channels, "ControlChannel", context, SocketType.ROUTER, hmacGenerator);
        this.channels = channels;
    }

    @Override
    @SuppressWarnings( {"rawtypes", "unchecked"})
    public void bind(ConnectionProperties connProps) {
        if (loopThread != null) {
            throw new IllegalStateException("Channel already bound.");
        }

        String channelThreadName = "Shell-" + ID.getAndIncrement();
        String addr = connProps.formatAddress(connProps.controlPort());

        LOGGER.info(logPrefix + "Binding to " + addr);
        socket.bind(addr);

        ZMQ.Poller poller = ctx.poller(1);
        poller.register(socket, ZMQ.Poller.POLLIN);

        this.loopThread = new LoopThread(channelThreadName, LOOP_SLEEP, () -> {
            if (poller.poll(0) > 0) {
                Message<?> message = readMessage();
                var type = message.header().type();
                MessageHandler handler = channels.getHandler(type);
                if (handler != null) {
                    LOGGER.info(logPrefix + "Handling message: " + type.name());
                    channels.setContext(message.getContext());
                    try {
                        handler.handle(message);
                    } catch (Exception e) {
                        LOGGER.severe(logPrefix + "Exception handling " + type.name() + ". " +
                                e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
                    } finally {
                        channels.runDelayedActions();
                    }
                    if (channels.isMarkedForShutdown()) {
                        LOGGER.info(channelThreadName + " shutting down connection marked for shutdown.");
                        channels.close();
                    }
                } else {
                    LOGGER.severe(logPrefix + "Unhandled message: " + type.name());
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
        if (this.loopThread != null) {
            try {
                this.loopThread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
