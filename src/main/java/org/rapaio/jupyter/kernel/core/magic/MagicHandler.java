package org.rapaio.jupyter.kernel.core.magic;

import java.util.List;

public interface MagicHandler {

    String name();

    boolean canHandleSnippet(String expr);

    Object eval(String expr) throws MagicParseException;

    String syntax();

    List<String> helpMessage();
}
