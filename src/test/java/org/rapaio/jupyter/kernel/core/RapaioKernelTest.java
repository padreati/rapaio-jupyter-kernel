package org.rapaio.jupyter.kernel.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.core.java.CompilerException;

public class RapaioKernelTest {

    @Test
    void compileErrorTest() {
        RapaioKernel rk = new RapaioKernel();
        String s = """
                // test
                %%jshell option
                line 1 of command
                line 2 of command
                """;
        CompilerException ce = assertThrows(CompilerException.class, () -> rk.eval(s));
        rk.formatException(ce);
    }
}
