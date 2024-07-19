package org.rapaio.jupyter.kernel.core.magic.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;

public class JarMagicHandlerTest {

    private JarMagicHandler handler;
    private RapaioKernel kernel;
    private Channels channels;

    @BeforeEach
    void beforeEach() {
        handler = new JarMagicHandler();
        kernel = new RapaioKernel();
        channels = Mockito.mock(Channels.class);
        kernel.registerChannels(channels);
    }

    @Test
    void testInvalidCode() {
        String code = "%%jars test";
        MagicEvalException ex = assertThrows(MagicEvalException.class, () -> handler.eval(kernel,
                new MagicSnippet(MagicSnippet.Type.MAGIC_CELL, List.of(new MagicSnippet.CodeLine(code, false, 0, 0)))));
        assertNotNull(ex);

        assertTrue(ex.hasErrorLine());
        assertEquals(0, ex.errorLine());
        assertEquals(6, ex.errorStart());
        assertEquals(code.length(), ex.errorEnd());
    }
}
