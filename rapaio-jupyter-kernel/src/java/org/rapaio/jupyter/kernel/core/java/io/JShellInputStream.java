package org.rapaio.jupyter.kernel.core.java.io;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.rapaio.jupyter.kernel.channels.Channels;

public class JShellInputStream extends InputStream {
    private static final Charset encoding = StandardCharsets.UTF_8;

    private final DynamicFIFOByteBuffer buffer = new DynamicFIFOByteBuffer();
    private Channels channels;
    private boolean enabled;

    public void bindChannels(Channels channels, boolean enabled) {
        this.channels = channels;
        this.enabled = enabled;
    }

    public void unbindChannels() {
        this.channels = null;
        this.enabled = false;
    }

    private void readFromFrontend() {
        if (enabled) {
            byte[] read = channels.readFromStdIn().getBytes(encoding);
            buffer.feed(read, read.length);
        }
    }

    @Override
    public synchronized int read() {
        if (!buffer.canTake(1)) {
            readFromFrontend();
        }
        if (buffer.canTake(1)) {
            byte[] data = buffer.take(1);
            return data[0] & 0xFF;
        }
        return -1;
    }

    @Override
    public int read(byte[] into, int intoOffset, int len) {
        Objects.requireNonNull(into, "Target buffer cannot be null");
        if (intoOffset < 0) {
            throw new IndexOutOfBoundsException("intoOffset must be semipositive");
        }
        if (len < 0) {
            throw new IndexOutOfBoundsException("len must be semipositive");
        }
        if (len > into.length - intoOffset) {
            throw new IndexOutOfBoundsException("There is not enough data to read.");
        }
        if (len == 0) {
            return 0;
        }

        if (!buffer.canTake(len)) {
            readFromFrontend();
        }

        int available = buffer.availableToTake();
        if(available==0) {
            return -1;
        }

        int actualLen = Math.min(available, len);
        byte[] data = buffer.take(actualLen);
        System.arraycopy(data, 0, into, intoOffset, actualLen);
        return actualLen;
    }

    @Override
    public int available() {
        return buffer.availableToTake();
    }
}
