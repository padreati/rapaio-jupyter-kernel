package org.rapaio.jupyter.kernel.message;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record Message<T>(
        List<byte[]> identities,
        Header<T> header,
        Header<?> parentHeader,
        Map<String, Object> metadata,
        T content,
        List<byte[]> blobs) {


    public Message(MessageContext<?> ctx, MessageType<T> type, Map<String, Object> metadata, T content, List<byte[]> blobs) {
        this(
                ctx != null ? ctx.identities() : Collections.emptyList(),
                new Header<>(ctx, type),
                ctx != null ? ctx.header() : null,
                metadata,
                content,
                blobs
        );
    }

    public MessageContext<T> getContext() {
        return new MessageContext<>(identities, header);
    }
}
