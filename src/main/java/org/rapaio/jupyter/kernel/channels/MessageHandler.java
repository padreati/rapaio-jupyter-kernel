package org.rapaio.jupyter.kernel.channels;


import org.rapaio.jupyter.kernel.message.Message;

@FunctionalInterface
public interface MessageHandler<T> {

    void handle(Message<T> message);
}
