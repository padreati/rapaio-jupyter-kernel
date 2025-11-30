package org.rapaio.jupyter.kernel.core.magic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.TestUtils;
import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.ExecutionContext;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.magic.interpolate.StringInterpolator;

public class StringInterpolatorTest {

    private ExecutionContext ctx;
    private RapaioKernel kernel;
    private Channels channels;
    private StringInterpolator interpolator;

    @BeforeEach
    void beforeEach() throws NoSuchAlgorithmException, InvalidKeyException {
        ctx = TestUtils.context();
        kernel = new RapaioKernel();
        channels = TestUtils.spyChannels();
        channels.connect(kernel);

        interpolator = new StringInterpolator();
    }

    @AfterEach
    void afterEach() {
        channels.close();
    }

    @Test
    void testSmoke() throws Exception {

        kernel.javaEngine().eval(ctx, "int a = 10;");
        kernel.javaEngine().eval(ctx, "int a = 100;");
        assertEquals("a100b\\\\{b}", interpolator.interpolate(ctx, kernel, "a\\{a}b\\\\{b}"));
        assertEquals("a100b\\\\{b}", interpolator.interpolate(ctx, kernel, "a\\{  a }b\\\\{b}"));

        Exception ex = assertThrows(Exception.class, () -> interpolator.interpolate(ctx, kernel, "a\\{  a }b\\{b}"));
        assertEquals("Interpolation string cannot be interpreted: b", ex.getMessage());
    }

    @Test
    void testToString() throws Exception {
        kernel.javaEngine().eval(ctx, "import java.time.*;");
        kernel.javaEngine().eval(ctx, "var a = Instant.parse(\"2007-12-03T10:15:30.00Z\");");
        assertEquals("2007-12-03T10:15:30Z", interpolator.interpolate(ctx, kernel, "\\{a}"));
    }

    @Test
    void testMethod() throws Exception {
        kernel.javaEngine().eval(ctx, "import java.time.*;");
        kernel.javaEngine().eval(ctx, "Instant a() { return Instant.parse(\"2007-12-03T10:15:30.00Z\"); }");
        kernel.javaEngine().eval(ctx, "int a = 10;");
        kernel.javaEngine().eval(ctx, "int add(int a, int b) { return a + b; }");
        assertEquals("20", interpolator.interpolate(ctx, kernel, "\\{add(a, 10)}"));
    }
}
