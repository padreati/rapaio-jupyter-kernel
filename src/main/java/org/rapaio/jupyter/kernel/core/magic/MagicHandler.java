package org.rapaio.jupyter.kernel.core.magic;

import java.util.List;

import org.rapaio.jupyter.kernel.channels.ReplyEnv;
import org.rapaio.jupyter.kernel.core.ReplacementOptions;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;

public interface MagicHandler {

    String name();

    String syntax();

    List<String> helpMessage();

    boolean canHandleSnippet(MagicSnippet snippet);

    Object eval(JavaEngine engine, ReplyEnv env, MagicSnippet snippet) throws MagicParseException;

    DisplayData inspect(ReplyEnv env, MagicSnippet snippet);

    ReplacementOptions complete(ReplyEnv env, MagicSnippet snippet);
}
