package org.rapaio.jupyter.kernel.channels;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.message.HMACDigest;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public final class HeartbeatChannel extends AbstractChannel {

    private static final Logger LOGGER = Logger.getLogger(HeartbeatChannel.class.getSimpleName());

    private static final long LOOP_SLEEP = 50;
    private static final AtomicInteger ID = new AtomicInteger();

    private volatile LoopThread loopThread;

    public HeartbeatChannel(ZMQ.Context context, HMACDigest hmacGenerator) {
        super("HeartbeatChannel", context, SocketType.REP, hmacGenerator);
    }

    @Override
    public void bind(ConnectionProperties connProps) {
        if (loopThread != null) {
            throw new IllegalStateException("Channel already bound.");
        }

        String channelThreadName = "Heartbeat-" + ID.getAndIncrement();
        String addr = connProps.formatAddress(connProps.hbPort());

        LOGGER.info(logPrefix + String.format("Binding %s to %s.", channelThreadName, addr));
        socket.bind(addr);

        ZMQ.Poller poller = ctx.poller(1);
        poller.register(socket, ZMQ.Poller.POLLIN);

        loopThread = new LoopThread(channelThreadName, LOOP_SLEEP, () -> {
            int events = poller.poll(0);
            if (events > 0) {
                byte[] msg = socket.recv();
                if (msg == null) {
                    // ignore errors, just show them
                    LOGGER.severe(logPrefix + "Poll returned 1 event but could not read the echo string");
                    return;
                }
                if (!socket.send(msg)) {
                    LOGGER.severe(logPrefix + "Could not send heartbeat reply");
                }
            }
        });
        loopThread.start();
        LOGGER.info(logPrefix + "Polling on " + channelThreadName);
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
