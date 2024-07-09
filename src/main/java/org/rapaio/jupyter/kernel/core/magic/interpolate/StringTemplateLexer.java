package org.rapaio.jupyter.kernel.core.magic.interpolate;

import java.util.ArrayList;
import java.util.List;

public class StringTemplateLexer implements StringLexer {

    private static final char ESCAPE = '\\';
    private static final char OPEN = '{';
    private static final char END = '}';

    @Override
    public List<StringToken> tokenize(String text) throws LexerParserException {

        List<StringToken> tokens = new ArrayList<>();

        int start = 0;
        while (start < text.length()) {
            StringToken token = collectToken(text, start);
            tokens.add(token);
            start += token.originalValue().length();
        }

        return tokens;
    }

    private StringToken collectToken(String text, int start) throws LexerParserException {
        // if start with interpolation
        if (start + 1 < text.length() && text.charAt(start) == ESCAPE && text.charAt(start + 1) == OPEN) {
            // read until interpolation end
            int current = start + 2;
            while (current < text.length()) {
                if (text.charAt(current) == END) {
                    String interpolation = text.substring(start, current + 1);
                    if (interpolation.length() <= 3) {
                        throw new LexerParserException("Interpolating reference is empty.", start, current + 1 - start);
                    }
                    return new StringToken(interpolation, true);
                }
                current++;
            }
            throw new LexerParserException("Interpolating reference is not closed.", start, text.length() - start);
        }

        if (text.length() - start < 3) {
            return new StringToken(text.substring(start), false);
        }
        int current = start + 2;
        while (current < text.length()) {
            if (text.charAt(current) != OPEN) {
                current++;
                continue;
            }
            if (text.charAt(current - 2) != ESCAPE && text.charAt(current - 1) == ESCAPE && text.charAt(current) != ESCAPE) {
                return new StringToken(text.substring(start, current - 1), false);
            }
            current++;
        }
        return new StringToken(text.substring(start), false);
    }
}
