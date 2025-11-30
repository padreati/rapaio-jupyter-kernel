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
        List<byte[]> buffers) {


    public Message(MessageId<?> msgId, MessageType<T> type, T content) {

        this(msgId != null ? msgId.identities() : Collections.emptyList(), new Header<>(msgId, type),
                msgId != null ? msgId.header() : null, null, content, null);
    }

    public Message(MessageType<T> type, T content) {
        this(Collections.emptyList(), new Header<>(null, type), null, null, content, null);
    }

    public MessageId<T> getId() {
        return new MessageId<>(identities, header);
    }
}
