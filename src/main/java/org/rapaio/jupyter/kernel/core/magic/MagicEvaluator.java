package org.rapaio.jupyter.kernel.core.magic;

import java.util.LinkedList;
import java.util.List;

import org.rapaio.jupyter.kernel.core.java.JavaEngine;

public class MagicEvaluator {

    public record MagicResult(boolean handled, Object result) {
    }

    private static final List<MagicHandler> magicHandlers = new LinkedList<>();

    static {
        // register magic handlers
        magicHandlers.add(new JavaReplMagicHandler());
        magicHandlers.add(new HelpMagicHandler(magicHandlers));
    }

    private final JavaEngine javaEngine;

    public MagicEvaluator(JavaEngine javaEngine) {
        this.javaEngine = javaEngine;
    }

    public MagicResult eval(String expr) throws MagicParseException {
        for (var handler : magicHandlers) {
            // first handler do the job
            if (handler.canHandleSnippet(expr)) {
                return new MagicResult(true, handler.eval(expr));
            }
        }
        return new MagicResult(false, null);
    }
}
