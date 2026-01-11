package org.rapaio.jupyter.display.provider.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExampleSetDisplayRendererTest {

    private ExampleSetDisplayRenderer renderer;

    @BeforeEach
    void beforeEach() {
        renderer = new ExampleSetDisplayRenderer();
    }

    @Test
    void emptySetTest() {
        Set<?> set = Set.of();

        var dd = renderer.render("text/plain", set);
        assertTrue(dd.data().containsKey("text/plain"));
        assertEquals("Example renderer: SetN{size:0,[]}", dd.data().get("text/plain"));

        dd = renderer.render("text/html", set);
        assertTrue(dd.data().containsKey("text/html"));
        assertEquals("Example renderer: <b>SetN</b>{size:0,[]}", dd.data().get("text/html"));
    }

    @Test
    void testHundredInteger() {
        Set<Integer> numbers = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            numbers.add(i);
        }
        var dd = renderer.render("text/plain", numbers);
        assertTrue(dd.data().containsKey("text/plain"));
        assertEquals("Example renderer: HashSet{size:100,[0,1,2,3,4,5,6,7,8,9, ...]}", dd.data().get("text/plain"));

        dd = renderer.render("text/html", numbers);
        assertTrue(dd.data().containsKey("text/html"));
        assertEquals("Example renderer: <b>HashSet</b>{size:100,[0,1,2,3,4,5,6,7,8,9, ...]}", dd.data().get("text/html"));
    }
}
