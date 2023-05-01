package org.rapaio.jupyter.kernel.core.java.magic;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.DisplayData;

public class MagicEvaluatorTest {

    @Test
    void magicHelpTest() throws Exception {
        RapaioKernel kernel = new RapaioKernel();
        DisplayData dd = kernel.eval("%help");
        System.out.println(dd);
    }
}
