package org.rapaio.jupyter.kernel.core.magic;

import org.rapaio.jupyter.kernel.core.RapaioKernel;

@FunctionalInterface
public interface OneLineMagicEvalFunction {

    Object eval(RapaioKernel kernel, MagicSnippet snippet) throws
            MagicEvalException, MagicParseException;
}
