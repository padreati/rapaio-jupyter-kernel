package org.rapaio.jupyter.kernel.core.magic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rapaio.jupyter.kernel.TestUtils;
import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.ExecutionContext;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.DisplayData;

public class MagicEngineTest {

    private ExecutionContext ctx;
    private RapaioKernel kernel;
    private Channels channels;

    @BeforeEach
    void beforeEach() {
        channels = Mockito.mock(Channels.class);
        ctx = TestUtils.context();
        kernel = new RapaioKernel();
        kernel.registerChannels(channels);
    }

    @Test
    void magicHelpTest() throws Exception {
        MagicEvalResult result = kernel.magicEngine().eval(new ExecutionContext(null), "%help");
        DisplayData dd = (DisplayData) result.result();
        assertNotNull(dd);
        assertNotNull(dd.data());
        assertTrue(dd.data().containsKey("text/plain"));
    }

    @Test
    void magicInspectJavaCodeTest() throws MagicEvalException, MagicParseException {
        var result = kernel.magicEngine().inspect(ctx, "System.out.print(\"test\");", 3);
        assertFalse(result.handled());
    }

    @Test
    void magicInspectMagicCode() throws MagicEvalException, MagicParseException {
        var result = kernel.magicEngine().inspect(ctx, "%load", 3);
        assertTrue(result.handled());
        DisplayData dd = result.displayData();
        assertNotNull(dd);
        assertNotNull(dd.data());
        assertTrue(dd.data().containsKey("text/html"));
    }

    @Test
    void magicCompleteTest() throws MagicEvalException, MagicParseException {
        var result = kernel.magicEngine().complete(kernel, """
                %%jars
                /""", 0);
        assertNotNull(result);
        assertFalse(result.replacementOptions().replacements().isEmpty());
        assertEquals(0, result.replacementOptions().start());
        assertEquals(6, result.replacementOptions().end());

        result = kernel.magicEngine().complete(kernel, "%depen", 1);
        assertNotNull(result);
        var options = result.replacementOptions();
        assertFalse(options.replacements().isEmpty());
        assertEquals(0, options.start());
        assertEquals(6, options.end());

        String line1 = "%dependency /add x:y:z\n";
        result = kernel.magicEngine().complete(kernel, line1 + "%depe", line1.length() + 1);
        assertNotNull(result);
        options = result.replacementOptions();
        assertFalse(options.replacements().isEmpty());
        assertEquals(line1.length(), options.start());
        assertEquals(line1.length() + "%depe" .length(), options.end());
    }

    @Test
    void magicInterpolationTest() {
        assertDoesNotThrow(() -> {
            kernel.javaEngine().eval(ctx, "String lib = \"com.github.javafaker:javafaker:1.0.2\";");
            kernel.magicEngine().eval(ctx, "%dependency /add \\{lib}");
            kernel.magicEngine().eval(ctx, "%dependency /resolve");
        });
    }
}
