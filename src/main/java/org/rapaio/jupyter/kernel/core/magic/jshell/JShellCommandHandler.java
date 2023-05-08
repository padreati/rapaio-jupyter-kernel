package org.rapaio.jupyter.kernel.core.magic.jshell;

import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;

public interface JShellCommandHandler {

    Object eval(MagicEngine magicEvaluator, JavaEngine javaEngine, Channels channels, MagicSnippet magicSnippet, String line) throws MagicEvalException;
}
