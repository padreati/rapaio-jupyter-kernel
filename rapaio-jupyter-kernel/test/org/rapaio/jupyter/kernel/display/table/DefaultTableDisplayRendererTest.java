package org.rapaio.jupyter.kernel.display.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.display.DisplayData;
import org.rapaio.jupyter.kernel.display.MimeType;
import org.rapaio.jupyter.kernel.global.Global;

class DefaultTableDisplayRendererTest {

    private final DefaultTableDisplayRenderer renderer = new DefaultTableDisplayRenderer();

    @BeforeEach
    void beforeEach() {
        Global.options().reset();
    }

    @AfterEach
    void afterEach() {
        Global.options().reset();
    }

    @Test
    void testCanRender() {
        assertTrue(renderer.canRender(null));
        assertTrue(renderer.canRender("html"));
        assertTrue(renderer.canRender("md"));
        assertFalse(renderer.canRender("text"));
        assertFalse(renderer.canRender("jpg"));
    }

    @Test
    void rendererClassTest() {
        assertEquals(TableDisplay.class, renderer.rendererClass());
    }

    @Test
    void renderNullObject() {
        assertNull(renderer.render("html", null));
    }

    @Test
    void assertOtherMimeType() {
        assertNull(renderer.render("text", new TestTableDisplay(2, 2, true)));
        assertNull(renderer.render("png", new TestTableDisplay(3, 3, true)));
    }

    @Test
    void testRenderHtml() {
        TestTableDisplay td = new TestTableDisplay(2, 2, true);

        DisplayData result = renderer.render(MimeType.HTML.toString(), td);

        assertNotNull(result);
        assertNotNull(result.getDisplayId());
        String html = result.data().get(MimeType.HTML.toString()).toString();

        System.out.println("\n" + html);

        assertTrue(html.contains("border=\"1\""));
        assertTrue(html.contains("id="));
        assertTrue(html.contains("<thead>"));
        assertTrue(html.contains("<tbody>"));
        assertTrue(html.contains("<th>0</th>"));
        assertTrue(html.contains("D[1,0]"));
    }

    @Test
    void testRenderMarkdown() {
        TestTableDisplay td = new TestTableDisplay(2, 2, true);

        DisplayData result = renderer.render(MimeType.MARKDOWN.toString(), td);

        assertNotNull(result);
        String markdown = result.data().get(MimeType.MARKDOWN.toString()).toString();

        assertTrue(markdown.contains("|  |Column[0] |Column[1] |"));
        assertTrue(markdown.contains("| --- |--- |--- |"));
        assertTrue(markdown.contains("| **0** |D[0,0] |D[0,1] |"));
        assertTrue(markdown.contains("| **1** |D[1,0] |D[1,1] |"));
    }

    @Test
    void testMaxRows() {
        TestTableDisplay td = new TestTableDisplay(1000, 1, true);

        Global.options().display().maxRows(1);

        String html = renderer.render(null, td).data().get(MimeType.HTML.toString()).toString();
        assertEquals(3, html.lines().filter(line -> line.contains("<tr>")).count());

        Global.options().display().maxRows(10);

        html = renderer.render(null, td).data().get(MimeType.HTML.toString()).toString();
        assertEquals(12, html.lines().filter(line -> line.contains("<tr>")).count());

        Global.options().display().maxRows(0);
        html = renderer.render(null, td).data().get(MimeType.HTML.toString()).toString();
        assertEquals(1001, html.lines().filter(line -> line.contains("<tr>")).count());
    }

    @Test
    void testMaxCols() {
        TestTableDisplay td = new TestTableDisplay(1, 1000, true);

        Global.options().display().maxCols(1);
        String html = renderer.render(null, td).data().get(MimeType.HTML.toString()).toString();
        assertEquals(1, html.lines().flatMap(line -> Arrays.stream(line.split("<"))).filter(line -> line.startsWith("td style")).count());

        Global.options().display().maxCols(10);
        html = renderer.render(null, td).data().get(MimeType.HTML.toString()).toString();
        assertEquals(10, html.lines().flatMap(line -> Arrays.stream(line.split("<"))).filter(line -> line.startsWith("td style")).count());

        Global.options().display().maxCols(0);
        html = renderer.render(null, td).data().get(MimeType.HTML.toString()).toString();
        assertEquals(1000, html.lines().flatMap(line -> Arrays.stream(line.split("<"))).filter(line -> line.startsWith("td style")).count());


        td = new TestTableDisplay(1000, 1000, true);
        Global.options().display().maxRows(1);
        Global.options().display().maxCols(1);

        html = renderer.render(null, td).data().get(MimeType.HTML.toString()).toString();
        assertEquals(3, html.lines().filter(line -> line.contains("<tr>")).count());
        assertEquals(1, html.lines().flatMap(line -> Arrays.stream(line.split("<"))).filter(line -> line.startsWith("td style")).count());
    }

}

