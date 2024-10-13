package org.rapaio.jupyter.kernel.global;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rapaio.jupyter.kernel.MainApp;
import org.rapaio.jupyter.kernel.TestUtils;
import org.rapaio.jupyter.kernel.channels.Channels;

public class GlobalTest {

    @Test
    void testMarkdown() throws NoSuchAlgorithmException, InvalidKeyException {
        Channels channels = TestUtils.spyChannels();
        MainApp.kernel.registerChannels(channels);

        Mockito.when(channels.hasMsgId()).thenReturn(true);
        Global.display("text/markdown", "**markdown**");
    }
}
