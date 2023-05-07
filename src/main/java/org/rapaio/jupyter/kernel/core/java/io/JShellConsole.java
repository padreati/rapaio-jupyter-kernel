package org.rapaio.jupyter.kernel.core.java.io;

import org.rapaio.jupyter.kernel.channels.ReplyEnv;

public final class JShellConsole {

    private final JShellOutputStream out;
    private final JShellOutputStream err;
    private final JShellInputStream in;

    public JShellConsole() {
        this.out = new JShellOutputStream(false);
        this.err = new JShellOutputStream(true);
        this.in = new JShellInputStream();
    }

    public void bindEnv(ReplyEnv env, boolean inputEnabled) {
        out.bindEnv(env);
        err.bindEnv(env);
        in.bindEnv(env, inputEnabled);
    }

    public void unbindEnv() {
        out.unbindEnv();
        err.unbindEnv();
        in.unbindEnv();
    }

    public void flush() {
        out.flush();
        err.flush();
    }

    public JShellInputStream getIn() {
        return in;
    }

    public JShellOutputStream getOut() {
        return out;
    }

    public JShellOutputStream getErr() {
        return err;
    }
}
