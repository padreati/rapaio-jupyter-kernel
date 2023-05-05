package org.rapaio.jupyter.kernel.channels;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.message.HMACDigest;
import org.rapaio.jupyter.kernel.util.Formatter;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class HeartbeatChannel extends AbstractChannel {

    private static final Logger LOGGER = Logger.getLogger(HeartbeatChannel.class.getSimpleName());

    private static final long HB_DEFAULT_SLEEP_MS = 50;
    private static final AtomicInteger ID = new AtomicInteger();

    private volatile LoopThread loopThread;

    public HeartbeatChannel(ZMQ.Context context, HMACDigest hmacGenerator) {
        super("HeartbeatChannel", context, SocketType.REP, hmacGenerator);
    }

    private boolean isBound() {
        return loopThread != null;
    }

    @Override
    public void bind(ConnectionProperties connProps) {
        if (this.isBound()) {
            throw new IllegalStateException("Heartbeat channel already bound");
        }

        String channelThreadName = "Heartbeat-" + ID.getAndIncrement();
        String addr = Formatter.formatAddress(connProps.transport(), connProps.ip(), connProps.hbPort());

        LOGGER.info(logPrefix + String.format("Binding %s to %s.", channelThreadName, addr));
        socket.bind(addr);

        ZMQ.Poller poller = ctx.poller(1);
        poller.register(socket, ZMQ.Poller.POLLIN);

        this.loopThread = new LoopThread(channelThreadName, HB_DEFAULT_SLEEP_MS, () -> {
            int events = poller.poll(0);
            if (events > 0) {
                byte[] msg = socket.recv();
                if (msg == null) {
                    //Error during receive, just continue
                    LOGGER.severe(logPrefix + "Poll returned 1 event but could not read the echo string");
                    return;
                }
                if (!socket.send(msg)) {
                    LOGGER.severe(logPrefix + "Could not send heartbeat reply");
                }
                LOGGER.finest(logPrefix + "Heartbeat pulse");
            }
        });
        this.loopThread.start();
        LOGGER.info(logPrefix + "Polling on " + channelThreadName);
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
        if (loopThread != null) {
            try {
                loopThread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
