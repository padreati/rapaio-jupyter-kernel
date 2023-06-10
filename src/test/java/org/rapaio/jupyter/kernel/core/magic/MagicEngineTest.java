package org.rapaio.jupyter.kernel.core.magic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.RapaioKernel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        MagicEvalResult result = kernel.evalMagic("%help");
        System.out.println(result.result());
    }

    @Test
    void magicInspectJavaCodeTest() throws MagicEvalException, MagicParseException {
        var result = kernel.magicEngine().inspect("System.out.print(\"test\");", 3);
        assertFalse(result.handled());
    }

    @Test
    void magicInspectMagicCode() throws MagicEvalException, MagicParseException {
        var result = kernel.magicEngine().inspect("%load", 3);
        assertTrue(result.handled());
        System.out.println(result.displayData());
    }

    @Test
    void magicCompleteTest() throws MagicEvalException, MagicParseException {
        var result = kernel.magicEngine().complete(kernel, """
                %%jars
                /""", 0);
        System.out.println(result);
    }

    @Test
    void magicJarsEvalTest() throws MagicEvalException, MagicParseException {
        kernel.magicEngine().eval("""
                %%jars
                /home/ati/work/rapaio-kaggle/
                /home/ati/work/rapaio/rapaio-core/target/
                """);
    }
}
