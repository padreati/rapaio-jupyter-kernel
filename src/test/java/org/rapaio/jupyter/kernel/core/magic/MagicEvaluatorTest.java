package org.rapaio.jupyter.kernel.core.magic;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.core.RapaioKernel;

public class MagicEvaluatorTest {

    @Test
    void magicHelpTest() throws Exception {
        RapaioKernel kernel = new RapaioKernel();
        MagicEvalResult result = kernel.evalMagic("%help");
        System.out.println(result.result());
    }
}
