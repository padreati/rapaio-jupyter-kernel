package org.rapaio.jupyter.kernel.core.magic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.ExecutionContext;
import org.rapaio.jupyter.kernel.core.RapaioKernel;

public class MagicEngineTest {

    private RapaioKernel kernel;
    private Channels channels;

    @BeforeEach
    void beforeEach() {
        channels = Mockito.mock(Channels.class);
        kernel = new RapaioKernel();
        kernel.registerChannels(channels);
    }

    @Test
    void magicHelpTest() throws Exception {
        MagicEvalResult result = kernel.magicEngine().eval(new ExecutionContext(null), "%help");
        System.out.println(result.result());
    }

    @Test
    void magicInspectJavaCodeTest() throws MagicEvalException, MagicParseException {
        var result = kernel.magicEngine().inspect(new ExecutionContext(null), "System.out.print(\"test\");", 3);
        assertFalse(result.handled());
    }

    @Test
    void magicInspectMagicCode() throws MagicEvalException, MagicParseException {
        var result = kernel.magicEngine().inspect(new ExecutionContext(null), "%load", 3);
        assertTrue(result.handled());
        System.out.println(result.displayData());
    }

    @Test
    void magicCompleteTest() throws MagicEvalException, MagicParseException {
        var result = kernel.magicEngine().complete(kernel, new ExecutionContext(null), """
                %%jars
                /""", 0);
        assertNotNull(result);
        assertFalse(result.replacementOptions().replacements().isEmpty());
        assertEquals(0, result.replacementOptions().start());
        assertEquals(6, result.replacementOptions().end());

        result = kernel.magicEngine().complete(kernel, new ExecutionContext(null), "%depen", 1);
        assertNotNull(result);
        var options = result.replacementOptions();
        assertFalse(options.replacements().isEmpty());
        assertEquals(0, options.start());
        assertEquals(6, options.end());
    }

    @Test
    void magicJarsEvalTest() throws MagicEvalException, MagicParseException {
        kernel.magicEngine().eval(new ExecutionContext(null), """
                %%jars
                /home/ati/work/rapaio-kaggle/
                /home/ati/work/rapaio/rapaio-core/target/
                """);
    }
}
