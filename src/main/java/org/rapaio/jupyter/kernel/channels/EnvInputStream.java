package org.rapaio.jupyter.kernel.channels;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class EnvInputStream extends InputStream {
    private static final Charset encoding = StandardCharsets.UTF_8;

    private final ReplyEnv env;
    private final boolean enabled;
    private byte[] data = null;
    private int bufferPos = 0;

    public EnvInputStream(ReplyEnv env, boolean enabled) {
        this.env = env;
        this.enabled = enabled;
    }

    private byte[] readFromFrontend() {
        if (this.enabled) {
            return env.readFromStdIn().getBytes(encoding);
        }
        return new byte[0];
    }

    @Override
    public synchronized int read() {
        if (this.data == null) {
            if (this.env != null) {
                //Buffer is empty and there is an environment to read from so
                //ask the frontend for input
                this.data = this.readFromFrontend();
                this.bufferPos = 0;
            } else {
                return -1;
            }
        }
        if (this.bufferPos >= this.data.length) {
            this.data = null;
            if (this.env != null && this.enabled) {
                this.data = this.readFromFrontend();
                this.bufferPos = 0;
            } else {
                return -1;
            }
        }

        return this.data[this.bufferPos++];
    }

    @Override
    public int read(byte[] into, int intoOffset, int len) {
        Objects.requireNonNull(into, "Target buffer cannot be null");

        if (intoOffset < 0)
            throw new IndexOutOfBoundsException("intoOffset must be >= 0 but was " + intoOffset);
        else if (len < 0)
            throw new IndexOutOfBoundsException("len must be >= 0 but was " + len);
        else if (len > into.length - intoOffset)
            throw new IndexOutOfBoundsException(String.format("Reading len (%d) bytes starting at %d would overflow the buffer.", len, intoOffset));

        // If the request for some reason asks for 0 bytes then we don't have
        // to do anything.
        if (len == 0)
            return 0;

        // If the first read "ends" then the entire read "ends". Otherwise
        // any extra we can batch into this read is great!
        int c = this.read();
        if (c == -1)
            return -1;

        // Save the first read character, the rest will start at `intoOffset + 1`.
        into[intoOffset] = (byte) c;

        // Check how much we can read without blocking.
        int available = this.available();

        // If no extra characters are available immediately then we will stop here
        // with only the single first character read.
        if (available <= 0)
            return 1;

        // If the entire `len` is available in the buffer then that is how much
        // we will read. Otherwise we only want to read the amount available so that
        // there is no extra blocking read.
        int amountToTakeFromBuffer = Math.min(available, len);

        System.arraycopy(
                // Copy from the buffered data starting at the current position.
                this.data, this.bufferPos,
                // Copy into the given buffer starting at `intoOffset + 1` because
                // we already read a single character. Don't worry about indexing
                // issues as these were checked at the start.
                into, intoOffset + 1,
                // Copy whatever amount we decided we could take without blocking
                // while remaining <= `len`.
                amountToTakeFromBuffer
        );

        // Make sure to mark the amount we have taken from the buffer.
        this.bufferPos += amountToTakeFromBuffer;

        // We have read what we copied into the buffer plus the initial single
        // character that was read.
        return amountToTakeFromBuffer + 1;
    }

    @Override
    public int available() {
        return (this.data != null ? this.data.length : 0) - this.bufferPos;
    }
}
