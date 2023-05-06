package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.util.List;

import org.rapaio.jupyter.kernel.channels.ReplyEnv;
import org.rapaio.jupyter.kernel.core.Replacements;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicEvaluator;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicParseException;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;

public class JShellMagicHandler implements MagicHandler {

    @Override
    public String name() {
        return "JShell Magic Handler";
    }

    @Override
    public List<String> syntax() {
        return null;
    }

    @Override
    public List<String> helpMessage() {
        return null;
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet snippet) {
        return false;
    }

    @Override
    public Object eval(MagicEvaluator magicEvaluator, JavaEngine engine, ReplyEnv env, MagicSnippet snippet) throws MagicParseException,
            MagicEvalException {
        return null;
    }

    @Override
    public Replacements complete(ReplyEnv env, MagicSnippet snippet) {
        return null;
    }
}
