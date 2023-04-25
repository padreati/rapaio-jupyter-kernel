package org.rapaio.jupyter.kernel.core.java.io;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.rapaio.jupyter.kernel.channels.ReplyEnv;

public class JShellOutputStream extends ByteArrayOutputStream {
    private static final int INITIAL_BUFFER_CAP = 1024;

    private final boolean err;
    private ReplyEnv env;

    public JShellOutputStream(boolean err) {
        super(INITIAL_BUFFER_CAP);
        this.err = err;
    }

    public void bindEnv(ReplyEnv env) {
        this.env = env;
    }

    public void unbindEnv() {
        this.env = null;
    }

    @Override
    public void flush() {
        String contents = new String(buf, 0, count, StandardCharsets.UTF_8);
        if (!contents.isEmpty()) {
            if (env == null) {
                throw new IllegalStateException("No bound env at flush.");
            }
            if (err) {
                env.writeToStdErr(contents);
            } else {
                env.writeToStdOut(contents);
            }
            reset();
        }
    }
}
