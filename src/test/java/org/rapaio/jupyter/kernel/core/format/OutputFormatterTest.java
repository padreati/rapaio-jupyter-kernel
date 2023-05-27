package org.rapaio.jupyter.kernel.core.format;

import jdk.jshell.EvalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.core.RapaioKernel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OutputFormatterTest {

    private RapaioKernel kernel;

    @BeforeEach
    void beforeEach() {
        kernel = new RapaioKernel();
    }

    @Test
    void testEvalException() {
        EvalException e = assertThrows(EvalException.class, () -> kernel.eval("""
                List<String> list = new ArrayList();
                String g(int p) {
                    return list.get(p);
                };
                g(0);"""));
        assertNotNull(e);
        List<String> output = ErrorFormatters.exceptionFormat(kernel, e);
        assertNotNull(output);

        for (String line : output) {
            System.out.println(line);
        }
    }
}
