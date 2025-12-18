package org.rapaio.jupyter.display.provider.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SetDisplayRendererTest {

    private SetDisplayRenderer renderer;

    @BeforeEach
    void beforeEach() {
        renderer = new SetDisplayRenderer();
    }

    @Test
    void emptySetTest() {
        Set<?> set = Set.of();

        var dd = renderer.render("text/plain", set);
        assertTrue(dd.data().containsKey("text/plain"));
        assertEquals("SetN{size:0,[]}", dd.data().get("text/plain"));

        dd = renderer.render("text/html", set);
        assertTrue(dd.data().containsKey("text/html"));
        assertEquals("<b>SetN</b>{size:<b>0</b>,[]}", dd.data().get("text/html"));
    }

    @Test
    void testHundredInteger() {
        Set<Integer> numbers = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            numbers.add(i);
        }
        var dd = renderer.render("text/plain", numbers);
        assertTrue(dd.data().containsKey("text/plain"));
        assertEquals("HashSet{size:100,[0,1,2,3,4,5,6,7,8,9, ...]}", dd.data().get("text/plain"));

        dd = renderer.render("text/html", numbers);
        assertTrue(dd.data().containsKey("text/html"));
        assertEquals("<b>HashSet</b>{size:<b>100</b>,[0,1,2,3,4,5,6,7,8,9, ...]}", dd.data().get("text/html"));
    }
}
