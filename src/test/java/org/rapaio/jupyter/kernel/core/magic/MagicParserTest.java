package org.rapaio.jupyter.kernel.core.magic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MagicParserTest {

    private final MagicParser parser = new MagicParser();

    @Test
    void emptyTest() {
        List<MagicSnippet> snippets = parser.parseSnippets("", 0);
        assertNotNull(snippets);
        assertTrue(snippets.isEmpty());

        snippets = parser.parseSnippets("""
                """, 0);
        assertNotNull(snippets);
        assertTrue(snippets.isEmpty());
    }

    @Test
    void testSingleLine() {
        List<MagicSnippet> snippets = parser.parseSnippets("""
                %line test
                """, 0);
        assertNotNull(snippets);
        assertEquals(1, snippets.size());
        assertEquals(MagicSnippet.Type.MAGIC_LINE, snippets.get(0).type());
    }

    @Test
    void testMultipleLine() {
        List<MagicSnippet> snippets = parser.parseSnippets("""
                %line 1
                // comment
                                
                %line 2
                // comment
                %line 3
                // comment
                                
                //comment
                """, 0);

        assertNotNull(snippets);
        assertEquals(3, snippets.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(MagicSnippet.Type.MAGIC_LINE, snippets.get(i).type());
            assertEquals("%line " + (i + 1), snippets.get(i).line(0).code());
            assertEquals(1, snippets.get(i).lines().size());
        }
    }

    @Test
    void testSingleCell() {
        List<MagicSnippet> snippets = parser.parseSnippets("""
                %%cell test
                line2
                """, 0);
        assertNotNull(snippets);
        assertEquals(1, snippets.size());
        assertEquals(MagicSnippet.Type.MAGIC_CELL, snippets.get(0).type());
    }

    @Test
    void testMultipleCells() {
        List<MagicSnippet> snippets = parser.parseSnippets("""
                %%cell test
                line2
                %%cell test2
                line1
                """, 0);
        assertNotNull(snippets);
        assertEquals(2, snippets.size());
        assertEquals(MagicSnippet.Type.MAGIC_CELL, snippets.get(0).type());
        assertEquals(MagicSnippet.Type.MAGIC_CELL, snippets.get(1).type());
    }

    @Test
    void testLineAndCellMagics() {
        List<MagicSnippet> snippets = parser.parseSnippets("""
                %line 1
                %%cell 1
                cell1
                                
                cell2
                //
                //
                cell3
                %line 2
                %%cell 2
                cell1
                                
                                
                cell2
                //
                cell3
                //
                                
                                
                """, 0);

        assertNotNull(snippets);
        assertEquals(4, snippets.size());
        assertEquals(MagicSnippet.Type.MAGIC_LINE, snippets.get(0).type());
        assertEquals("%line 1", snippets.get(0).line(0).code());
        assertEquals(MagicSnippet.Type.MAGIC_CELL, snippets.get(1).type());
        assertEquals("%%cell 1", snippets.get(1).line(0).code());
        assertEquals("cell1", snippets.get(1).line(1).code());
        assertEquals("cell2", snippets.get(1).line(2).code());
        assertEquals("cell3", snippets.get(1).line(3).code());
        assertEquals(MagicSnippet.Type.MAGIC_LINE, snippets.get(2).type());
        assertEquals("%line 2", snippets.get(2).line(0).code());
        assertEquals(MagicSnippet.Type.MAGIC_CELL, snippets.get(3).type());
        assertEquals("%%cell 2", snippets.get(3).line(0).code());
        assertEquals("cell1", snippets.get(3).line(1).code());
        assertEquals("cell2", snippets.get(3).line(2).code());
        assertEquals("cell3", snippets.get(3).line(3).code());

        assertFalse(parser.containsMixedSnippets(snippets));
    }

    @Test
    void testMixedMagicAndOther() {
        List<MagicSnippet> snippets = parser.parseSnippets("""
                %line 1
                record C {
                }
                %line 2
                """, 0);
        assertNotNull(snippets);
        assertEquals(3, snippets.size());
        assertArrayEquals(new MagicSnippet.Type[]
                        {MagicSnippet.Type.MAGIC_LINE, MagicSnippet.Type.NON_MAGIC, MagicSnippet.Type.MAGIC_LINE},
                snippets.stream().map(MagicSnippet::type).toArray());

        assertTrue(parser.containsMixedSnippets(snippets));
    }
}
