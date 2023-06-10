package org.rapaio.jupyter.kernel.global;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rapaio.jupyter.kernel.MainApp;
import org.rapaio.jupyter.kernel.channels.Channels;

public class GlobalTest {

    @Test
    void testMarkdown() {
        Channels channels = Mockito.mock(Channels.class);
        MainApp.kernel.registerChannels(channels);

        Mockito.when(channels.hasMsgId()).thenReturn(true);
        Global.display("text/markdown", "**markdown**");
    }
}
