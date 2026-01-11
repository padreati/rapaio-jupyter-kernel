package org.rapaio.jupyter.kernel.core.magic.interpolate;

import java.util.List;

public interface StringLexer {

    List<StringToken> tokenize(String text) throws InterpolationException;
}
