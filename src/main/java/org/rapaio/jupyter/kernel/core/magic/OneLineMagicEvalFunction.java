package org.rapaio.jupyter.kernel.core.magic;

import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;

@FunctionalInterface
public interface OneLineMagicEvalFunction {

    Object eval(MagicEngine magicEngine, JavaEngine javaEngine, Channels channels, MagicSnippet snippet) throws
            MagicEvalException, MagicParseException;
}
