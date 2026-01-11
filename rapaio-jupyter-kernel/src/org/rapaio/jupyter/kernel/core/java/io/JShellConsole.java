package org.rapaio.jupyter.kernel.core.java.io;

import java.io.InputStream;
import java.io.OutputStream;

import org.rapaio.jupyter.kernel.channels.Channels;

public class JShellConsole {

    private final JShellOutputStream out;
    private final JShellOutputStream err;
    private final JShellInputStream in;

    public JShellConsole() {
        this.out = new JShellOutputStream(false);
        this.err = new JShellOutputStream(true);
        this.in = new JShellInputStream();
    }

    public void bindChannels(Channels channels, boolean inputEnabled) {
        in.bindChannels(channels, inputEnabled);
        out.bindChannels(channels);
        err.bindChannels(channels);
    }

    public void unbindChannels() {
        in.unbindChannels();
        out.unbindChannels();
        err.unbindChannels();
    }

    public void flush() {
        out.flush();
        err.flush();
    }

    public InputStream getIn() {
        return in;
    }

    public OutputStream getOut() {
        return out;
    }

    public OutputStream getErr() {
        return err;
    }
}
