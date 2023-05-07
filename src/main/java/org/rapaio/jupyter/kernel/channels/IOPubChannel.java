package org.rapaio.jupyter.kernel.channels;

import java.util.logging.Logger;

import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.message.HMACDigest;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public final class IOPubChannel extends AbstractChannel {

    private static final Logger LOGGER = Logger.getLogger(IOPubChannel.class.getSimpleName());

    public IOPubChannel(ZMQ.Context context, HMACDigest hmacGenerator) {
        super("IOPubChannel", context, SocketType.PUB, hmacGenerator);
    }

    @Override
    public void bind(ConnectionProperties connProps) {
        String addr = connProps.formatAddress(connProps.iopubPort());
        LOGGER.info(logPrefix + String.format("Binding iopub to %s.", addr));
        socket.bind(addr);
    }
}
