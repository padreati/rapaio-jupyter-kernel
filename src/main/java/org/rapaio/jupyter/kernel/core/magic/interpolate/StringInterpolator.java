package org.rapaio.jupyter.kernel.core.magic.interpolate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.rapaio.jupyter.kernel.core.ExecutionContext;
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

    public final String interpolate(ExecutionContext context, RapaioKernel kernel, String text) throws Exception {
        return interpolate(context, kernel, text, defaultType);
    }

    public final String interpolate(ExecutionContext context, RapaioKernel kernel, String text, Type type) throws Exception {
        List<StringToken> tokens = lexerMap.get(type).tokenize(text);
        StringBuilder sb = new StringBuilder();
        for (StringToken token : tokens) {
            sb.append(interpolate(context, kernel, token));
        }
        return sb.toString();
    }


    public String interpolate(ExecutionContext context, RapaioKernel kernel, StringToken token) throws Exception {
        if (!token.canInterpolate()) {
            return token.originalValue();
        }
        String tokenValue = token.innerValue().trim();
        try {
            Object obj = kernel.javaEngine().eval(context, tokenValue);
            if (Objects.isNull(obj)) {
                return "null";
            }
            return Objects.toString(obj);
        }catch (Exception ex) {
            throw new InterpolationException("Interpolation string cannot be interpreted: " + tokenValue,
                    token.originalPosition(), token.originalValue().length());
        }
    }

}
