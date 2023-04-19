package org.rapaio.jupyter.kernel.channels;

import org.rapaio.jupyter.kernel.message.MessageType;

public interface KernelMessageHandler {

    void registerChannels(JupyterChannels jupyterChannels);

    <T> MessageHandler<T> getHandler(MessageType<T> type);
}
