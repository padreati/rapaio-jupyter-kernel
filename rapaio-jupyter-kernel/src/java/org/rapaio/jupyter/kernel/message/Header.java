package org.rapaio.jupyter.kernel.message;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

public record Header<T>(
        @SerializedName("id") String id,
        @SerializedName("username") String username,
        @SerializedName("session") String sessionId,
        @SerializedName("date") String timestamp,
        @SerializedName("msg_type") MessageType<T> type,
        @SerializedName("version") String version) {

    public static final String KERNEL_USERNAME = "kernel";
    public static final String PROTOCOL_VERSION = "5.4";

    private static final ThreadLocal<DateTimeFormatter> DATE_FORMAT = ThreadLocal.withInitial(
            () -> DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmZ").withZone(TimeZone.getTimeZone("UTC").toZoneId()));

    public Header(MessageId<?> msgId, MessageType<T> type) {
        this(
                UUID.randomUUID().toString(),
                msgId != null ? msgId.header().username() : KERNEL_USERNAME,
                msgId != null ? msgId.header().sessionId() : null,
                DATE_FORMAT.get().format(Instant.now()),
                type,
                PROTOCOL_VERSION
        );
    }
}
