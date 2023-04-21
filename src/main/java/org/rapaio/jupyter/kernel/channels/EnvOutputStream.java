package org.rapaio.jupyter.kernel.channels;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class EnvOutputStream extends ByteArrayOutputStream {
    private static final int INITIAL_BUFFER_CAP = 1024;

    private final Consumer<String> writeDelegate;

    public EnvOutputStream(Consumer<String> writeDelegate) {
        super(INITIAL_BUFFER_CAP);
        this.writeDelegate = writeDelegate;
    }

    @Override
    public void flush() {
        String contents = new String(buf, 0, count, StandardCharsets.UTF_8);
        if (!contents.isEmpty()) {
            writeDelegate.accept(contents);
        }
        reset();
    }
}
