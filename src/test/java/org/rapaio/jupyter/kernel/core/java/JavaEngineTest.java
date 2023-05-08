package org.rapaio.jupyter.kernel.core.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rapaio.jupyter.kernel.TestUtils;
import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.java.io.JShellConsole;

public class JavaEngineTest {

    @Test
    void buildTest() throws Exception {
        JavaEngine engine = JavaEngine.builder(TestUtils.getTestJShellConsole())
                .withTimeoutMillis(-1L)
                .build();

        var result = engine.eval("int x = 3;int y = 4;y");
        assertNotNull(result);
        assertTrue(result instanceof Integer);
        assertEquals(4, result);

        result = engine.eval("x");
        assertNotNull(result);
        assertTrue(result instanceof Integer);
        assertEquals(3, result);
    }

    @Test
    void completionTest() {
        JavaEngine engine = JavaEngine.builder(TestUtils.getTestJShellConsole())
                .withTimeoutMillis(-1L)
                .build();
        var replacements = engine.complete("Sys", 3);
        System.out.println(replacements);
    }

    @Test
    void printerTest() throws Exception {
        JavaEngine engine = JavaEngine.builder(TestUtils.getTestJShellConsole())
                .withTimeoutMillis(-1L)
                .build();
        var out = engine.eval("System.out.println(\"test\")");
        assertNull(out);
    }
}
