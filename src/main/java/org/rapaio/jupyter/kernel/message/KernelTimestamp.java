package org.rapaio.jupyter.kernel.message;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class KernelTimestamp {

    public static KernelTimestamp now() {
        return new KernelTimestamp(null);
    }

    private static final ThreadLocal<DateTimeFormatter> DATE_FORMAT = ThreadLocal.withInitial(
            () -> DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmZ").withZone(TimeZone.getTimeZone("UTC").toZoneId()));

    private final String serialized;

    public KernelTimestamp(String serialized) {
        this.serialized = (serialized != null) ? serialized : DATE_FORMAT.get().format(Instant.now());
    }

    public String getDateString() {
        return serialized;
    }
}
