package org.rapaio.jupyter.kernel;

import org.mockito.Mockito;
import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.core.java.io.JShellConsole;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class TestUtils {

    public static ConnectionProperties testConnectionProperties() {
        return new ConnectionProperties(60001, 60002, "tcp", null, 60003, 60004, "127.0.0.1", 60005, null);
    }

    public static JShellConsole getTestJShellConsole() {
        return new TestShellConsole();
    }

    public static Channels spyChannels() throws NoSuchAlgorithmException, InvalidKeyException {
        return Mockito.spy(new Channels(testConnectionProperties()));
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