package org.rapaio.jupyter.kernel.core.magic;

import org.rapaio.jupyter.kernel.core.java.JavaEngine;

public class MagicEvaluator {

    private final JavaEngine javaEngine;

    public MagicEvaluator(JavaEngine javaEngine) {
        this.javaEngine = javaEngine;
    }

    public Object eval(String expr) {
        return "magic";
    }
}
