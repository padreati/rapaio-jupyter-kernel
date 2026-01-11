package org.rapaio.jupyter.kernel.core.magic;

import org.rapaio.jupyter.kernel.core.RapaioKernel;

@FunctionalInterface
public interface MagicFunction<T> {

    T apply(RapaioKernel kernel, MagicSnippet snippet) throws
            MagicEvalException, MagicParseException;
}
