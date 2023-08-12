package org.rapaio.jupyter.kernel.core.magic;

import org.rapaio.jupyter.kernel.core.ExecutionContext;
import org.rapaio.jupyter.kernel.core.RapaioKernel;

@FunctionalInterface
public interface MagicFunction<T> {

    T apply(RapaioKernel kernel, ExecutionContext executionContext, MagicSnippet snippet) throws
            MagicEvalException, MagicParseException;
}
