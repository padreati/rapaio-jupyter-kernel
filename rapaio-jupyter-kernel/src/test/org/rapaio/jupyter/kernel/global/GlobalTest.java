package org.rapaio.jupyter.kernel.global;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rapaio.jupyter.kernel.MainApp;
import org.rapaio.jupyter.kernel.TestUtils;
import org.rapaio.jupyter.kernel.channels.Channels;

public class GlobalTest {

    private Channels channels;

    @BeforeEach
    void beforeEach() throws NoSuchAlgorithmException, InvalidKeyException {
        channels = TestUtils.spyChannels();
        channels.connect(MainApp.kernel);
    }

    @AfterEach
    void afterEach() {
        channels.close();
    }

    @Test
    void testMarkdown() {
        Mockito.when(channels.hasMsgId()).thenReturn(true);
        Global.display("text/markdown", "**markdown**");
    }
}
