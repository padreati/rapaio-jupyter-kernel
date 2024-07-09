package org.rapaio.jupyter.kernel.core.magic.interpolate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.rapaio.jupyter.kernel.core.RapaioKernel;

/**
 * This class implements interpolation of variables and eventual other calls into a string.
 * The string could come from various sources.
 */
public class StringInterpolator {

    public enum Type {
        // string template style interpolator; leave room for other interpretations
        STRING_TEMPLATE
    }

    private static final Map<Type, StringLexer> lexerMap = new HashMap<>();

    static {
        lexerMap.put(Type.STRING_TEMPLATE, new StringTemplateLexer());
    }

    private final Type defaultType = Type.STRING_TEMPLATE;

    public final String interpolate(RapaioKernel kernel, String text) throws LexerParserException {
        return interpolate(kernel, text, defaultType);
    }

    public final String interpolate(RapaioKernel kernel, String text, Type type) throws LexerParserException {
        List<StringToken> tokens = lexerMap.get(type).tokenize(text);
        return tokens.stream().map(token -> token.interpolate(kernel)).collect(Collectors.joining());
    }
}
