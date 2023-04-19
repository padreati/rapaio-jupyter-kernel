package org.rapaio.jupyter.kernel.base.message;

public interface ContentType<T> {
    MessageType<T> type();
}
