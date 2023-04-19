package org.rapaio.jupyter.kernel.message;

public interface ContentType<T> {
    MessageType<T> type();
}
