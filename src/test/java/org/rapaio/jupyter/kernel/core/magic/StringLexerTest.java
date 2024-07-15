package org.rapaio.jupyter.kernel.core.magic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.core.magic.interpolate.InterpolationException;
import org.rapaio.jupyter.kernel.core.magic.interpolate.StringLexer;
import org.rapaio.jupyter.kernel.core.magic.interpolate.StringTemplateLexer;
import org.rapaio.jupyter.kernel.core.magic.interpolate.StringToken;

public class StringLexerTest {

    @Test
    void testDefaultLexer() throws InterpolationException {

        StringLexer lexer = new StringTemplateLexer();
        assertTokenEquals(List.of(), lexer.tokenize(""));
        assertTokenEquals(List.of(StringToken.text("a", 0)), lexer.tokenize("a"));
        assertTokenEquals(List.of(StringToken.interpolate("\\{a}", 0)), lexer.tokenize("\\{a}"));
        assertTokenEquals(List.of(
                StringToken.text("a", 0),
                StringToken.interpolate("\\{A}", 1),
                StringToken.text(" b ", 5),
                StringToken.interpolate("\\{ c }", 8)
        ), lexer.tokenize("a\\{A} b \\{ c }"));

        InterpolationException ex = assertThrows(InterpolationException.class, () -> lexer.tokenize("\\{}"));
        assertEquals("Interpolating reference is empty.", ex.getMessage());
        assertEquals(0, ex.position());
        assertEquals(3, ex.length());
    }

    void assertTokenEquals(List<StringToken> expected, List<StringToken> actual) {
        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }
}
