package org.rapaio.jupyter.kernel.core.magic;

import static org.rapaio.jupyter.kernel.core.display.html.Tags.b;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.each;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.join;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.p;
import static org.rapaio.jupyter.kernel.core.display.html.Tags.texts;

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

    Object eval(MagicEvaluator magicEvaluator, JavaEngine engine, ReplyEnv env, MagicSnippet snippet) throws MagicParseException, MagicEvalException;

    default DisplayData inspect(ReplyEnv env, MagicSnippet snippet) {
        return DisplayData.withHtml(
                join(
                        p(texts("Syntax: "), b(texts(syntax()))),
                        p(each(helpMessage(), line -> p(texts(line))))
                ).render()
        );
    }

    ReplacementOptions complete(ReplyEnv env, MagicSnippet snippet);
}
