package org.rapaio.jupyter.kernel;

import java.io.InputStream;
import java.io.OutputStream;

import org.mockito.Mockito;
import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.core.java.io.JShellConsole;

public class TestUtils {

    public static ConnectionProperties testConnectionProperties() {
        return new ConnectionProperties(1, 2, "tdp", null, 3, 4, "127.0.0.1", 5, null);
    }

    public static JShellConsole getTestJShellConsole() {
        return new TestShellConsole();
    }

    public static Channels mockChannels() {
        return Mockito.mock(Channels.class);
    }
}

class TestShellConsole extends JShellConsole {

    @Override
    public InputStream getIn() {
        return System.in;
    }

    @Override
    public OutputStream getErr() {
        return System.err;
    }

    @Override
    public OutputStream getOut() {
        return System.out;
    }
}