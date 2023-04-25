package org.rapaio.jupyter.kernel.core.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class JavaEngineTest {

    @Test
    void validBuildTest() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> JavaEngine
                .builder()
                .build());
        assertEquals("Timeout must be specified.", e.getMessage());
    }

    @Test
    void buildTest() throws Exception {
        JavaEngine engine = JavaEngine.builder()
                .withTimeoutMillis(-1L)
                .build();

        var result = engine.eval("int x = 3;int y = 4;");
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
        JavaEngine engine = JavaEngine.builder()
                .withTimeoutMillis(-1L)
                .build();
        var replacements = engine.complete("Sys", 3);
        System.out.println(replacements);
    }
}
