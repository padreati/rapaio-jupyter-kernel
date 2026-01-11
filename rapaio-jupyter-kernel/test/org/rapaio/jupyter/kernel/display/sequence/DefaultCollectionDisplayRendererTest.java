package org.rapaio.jupyter.kernel.display.sequence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.display.MimeType;

public class DefaultCollectionDisplayRendererTest {

    private static final DefaultCollectionDisplayRenderer renderer = new DefaultCollectionDisplayRenderer();

    @Test
    void testCanRender() {
        assertTrue(renderer.canRender("html"));
        assertTrue(renderer.canRender("md"));
        assertTrue(renderer.canRender("text"));

        assertFalse(renderer.canRender("jpeg"));
    }

    @Test
    void testRenderClass() {
        assertEquals(Collection.class, renderer.rendererClass());
    }

    @Test
    void testEmptyList() {
        String text = renderer.render("text", List.of()).data().get(MimeType.TEXT.longType()).toString();
        assertEquals("ListN{size:0,[]}", text);

        text = renderer.render("html", List.of()).data().get(MimeType.HTML.longType()).toString();
        assertEquals("ListN{size:0,[]}", text);
    }

    @Test
    void testText() {
        var text = renderer.render("text", List.of(1, 2, 3, 4, 5)).data().get(MimeType.TEXT.longType()).toString();
        assertEquals("ListN{size:5,[1,2,3,4,5]}", text);

        text = renderer.render("text", IntStream.range(0, 1000).boxed().toList()).data().get(MimeType.TEXT.longType()).toString();
        assertEquals(
                "ListN{size:1000,[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49, ...]}",
                text);

        text = renderer.render("text", Set.of(1, 3, 2, 4, 5, 6, 7, 8, 9)).data().get(MimeType.TEXT.longType()).toString();
        assertTrue(text.startsWith("SetN{size:9,["));
        assertEquals("SetN{size:9,[1,2,3,4,5,6,7,8,9]}".length(), text.length());
    }

    @Test
    void testTextVsHtml() {
        var text = renderer.render("text", List.of(1, 2, 3, 4, 5)).data().get(MimeType.TEXT.longType()).toString();
        var html = renderer.render("html", List.of(1, 2, 3, 4, 5)).data().get(MimeType.HTML.longType()).toString();
        assertEquals(text, html);
    }

    @Test
    void testTextVsMarkdown() {
        var text = renderer.render("text", List.of(1, 2, 3, 4, 5)).data().get(MimeType.TEXT.longType()).toString();
        var md = renderer.render("md", List.of(1, 2, 3, 4, 5)).data().get(MimeType.MARKDOWN.longType()).toString();
        assertEquals(text, md);
    }
}
