package org.rapaio.jupyter.kernel.core.magic;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.TestUtils;
import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.magic.interpolate.LexerParserException;
import org.rapaio.jupyter.kernel.core.magic.interpolate.StringInterpolator;
import org.rapaio.jupyter.kernel.core.magic.interpolate.StringLexer;
import org.rapaio.jupyter.kernel.core.magic.interpolate.StringTemplateLexer;

public class StringInterpolatorTest {

    private RapaioKernel kernel;
    private Channels channels;
    private StringInterpolator interpolator;

    @BeforeEach
    void beforeEach() throws NoSuchAlgorithmException, InvalidKeyException {
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
    void testSmoke() throws LexerParserException {

        System.out.println(String.join(",", interpolator.interpolate(kernel, "a\\{a}b\\\\{b}")));

    }

    @Test
    void testLexer() throws LexerParserException {
        StringLexer lexer = new StringTemplateLexer();
    }
}
