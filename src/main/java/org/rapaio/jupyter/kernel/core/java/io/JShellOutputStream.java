package org.rapaio.jupyter.kernel.core.java.io;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.rapaio.jupyter.kernel.channels.Channels;

public class JShellOutputStream extends ByteArrayOutputStream {
    private static final int INITIAL_BUFFER_CAP = 1024;

    private final boolean err;
    private Channels channels;

    public JShellOutputStream(boolean err) {
        super(INITIAL_BUFFER_CAP);
        this.err = err;
    }

    public void bindChannels(Channels channels) {
        this.channels = channels;
    }

    public void unbindChannels() {
        this.channels = null;
    }

    @Override
    public void flush() {
        String contents = new String(buf, 0, count, StandardCharsets.UTF_8);
        if (!contents.isEmpty()) {
            if (channels == null) {
                throw new IllegalStateException("No bound env at flush.");
            }
            if (err) {
                channels.writeToStdErr(contents);
            } else {
                channels.writeToStdOut(contents);
            }
            reset();
        }
    }
}
