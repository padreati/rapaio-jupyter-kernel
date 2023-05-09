package org.rapaio.jupyter.kernel.core.format;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.core.RapaioKernel;

import jdk.jshell.EvalException;

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
        List<String> output = OutputFormatter.exceptionFormat(kernel.javaEngine(), e);
        assertNotNull(output);

        for (String line : output) {
            System.out.println(line);
        }
    }
}
