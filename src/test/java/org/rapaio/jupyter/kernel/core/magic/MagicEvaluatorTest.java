package org.rapaio.jupyter.kernel.core.magic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.core.RapaioKernel;

public class MagicEvaluatorTest {

    private RapaioKernel kernel;

    @BeforeEach
    void beforeEach() {
        kernel = new RapaioKernel();
    }

    @Test
    void magicHelpTest() throws Exception {
        MagicEvalResult result = kernel.evalMagic("%help");
        System.out.println(result.result());
    }

    @Test
    void magicInspectJavaCodeTest() {
        var result = kernel.magicEngine().inspect("System.out.print(\"test\");", 3);
        assertFalse(result.handled());
    }

    @Test
    void magicInspectMagicCode() {
        var result = kernel.magicEngine().inspect("%load", 3);
        assertTrue(result.handled());
    }
}
