package org.rapaio.jupyter.display.provider.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExampleListDisplayRendererTest {

    private ExampleListDisplayRenderer renderer;

    @BeforeEach
    void beforeEach() {
        renderer = new ExampleListDisplayRenderer();
    }

    @Test
    void testEmpty() {
        List<?> list = List.of();

        var dd = renderer.render("text/plain", list);
        assertTrue(dd.data().containsKey("text/plain"));
        assertEquals("Example renderer: ListN{size:0,[]}", dd.data().get("text/plain"));

        dd = renderer.render("text/html", list);
        assertTrue(dd.data().containsKey("text/html"));
        assertEquals("Example renderer: <b>ListN</b>{size:0,[]}", dd.data().get("text/html"));
    }

    @Test
    void testHundredInteger() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            numbers.add(i);
        }
        var dd = renderer.render("text/plain", numbers);
        assertTrue(dd.data().containsKey("text/plain"));
        assertEquals("Example renderer: ArrayList{size:100,[0,1,2,3,4,5,6,7,8,9, ...]}", dd.data().get("text/plain"));

        dd = renderer.render("text/html", numbers);
        assertTrue(dd.data().containsKey("text/html"));
        assertEquals("Example renderer: <b>ArrayList</b>{size:100,[0,1,2,3,4,5,6,7,8,9, ...]}", dd.data().get("text/html"));
    }
}
