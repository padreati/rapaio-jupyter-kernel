package org.rapaio.jupyter.kernel.core.magic.jshell;

import org.rapaio.jupyter.kernel.channels.ReplyEnv;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicEvaluator;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;

public interface JShellCommandHandler {

    Object eval(MagicEvaluator magicEvaluator, JavaEngine javaEngine, ReplyEnv env, MagicSnippet magicSnippet, String line) throws MagicEvalException;
}
