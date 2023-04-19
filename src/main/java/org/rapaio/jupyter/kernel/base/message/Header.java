package org.rapaio.jupyter.kernel.base.message;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;

public record Header<T>(
        @SerializedName("id") String id,
        @SerializedName("username") String username,
        @SerializedName("session") String sessionId,
        @SerializedName("date") KernelTimestamp timestamp,
        @SerializedName("msg_type") MessageType<T> type,
        @SerializedName("version") String version) {

    public static final String KERNEL_USERNAME = "kernel";
    public static final String PROTOCOL_VERSION = "5.4";


    public Header(MessageContext<?> ctx, MessageType<T> type) {
        this(
                UUID.randomUUID().toString(),
                ctx != null ? ctx.header().username() : KERNEL_USERNAME,
                ctx != null ? ctx.header().sessionId() : null,
                KernelTimestamp.now(),
                type,
                PROTOCOL_VERSION
        );
    }
}
