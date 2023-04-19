package org.rapaio.jupyter.kernel.message;

import java.util.Arrays;
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

    public boolean hasParentHeader() {
        return parentHeader != null;
    }

    public boolean hasMetadata() {
        return metadata != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Message {\n");
        sb.append("\tidentities = [\n");
        for (byte[] id : identities) {
            sb.append("\t\t").append(Arrays.toString(id)).append("\n");
        }
        sb.append("\t]\n");
        sb.append("\theader = ").append(header).append("\n");
        sb.append("\tparentHeader = ").append(parentHeader).append("\n");
        sb.append("\tmetadata = ").append(metadata).append("\n");
        sb.append("\tcontent = ").append(content).append("\n");
        sb.append("\tblobs = [\n");
        if (blobs != null) {
            for (byte[] blob : blobs) {
                sb.append("\t\t").append(Arrays.toString(blob)).append("\n");
            }
        }
        sb.append("\t]\n");
        sb.append("}\n");
        return sb.toString();
    }
}
