package org.rapaio.jupyter.kernel.display.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.display.DisplaySystem;
import org.rapaio.jupyter.kernel.global.Global;

public class MapTableTransformerTest {

    private final org.rapaio.jupyter.kernel.display.table.MapTableTransformer
            transformer = new org.rapaio.jupyter.kernel.display.table.MapTableTransformer();

    @Test
    void testClass() {
        assertEquals(TableDisplay.class, transformer.transformedClass());
    }

    @Test
    void testCanTransform() {
        assertFalse(transformer.canTransform(new Object()));
        assertTrue(transformer.canTransform(Map.of()));
    }

    @Test
    void testEmptyMap() {
        var model = transformer.transform(Map.of());
        assertEquals(0, model.getRows());
        assertEquals(2, model.getCols());
        assertTrue(model.hasHeader());
    }

    @Test
    void testSmallMap() {
        SortedMap<String, Integer> map = new TreeMap<>(Map.of("a", 1, "b", 2));
        var model = transformer.transform(map);
        assertEquals(2, model.getRows());
        assertEquals(2, model.getCols());
        assertTrue(model.hasHeader());
        assertEquals(DataType.STRING, model.columnType(0));
        assertEquals(DataType.INTEGER, model.columnType(1));

        assertEquals("Key", model.columnName(0));
        assertEquals("a", model.getFormattedValue(0, 0));
        assertEquals("b", model.getFormattedValue(1, 0));
        assertEquals("Value", model.columnName(1));
        assertEquals("1", model.getFormattedValue(0, 1));
        assertEquals("2", model.getFormattedValue(1, 1));
    }

    @Test
    void mapTransformerPipeline() {
        DisplaySystem.inst().refreshSpiDisplayHandlers();
        var dd = DisplaySystem.inst().render(new TreeMap<>(Map.of("a", 1, "b", 2)));

        String mime = Global.options().display().defaultMime();
        assertTrue(dd.data().containsKey(mime));
        assertTrue(dd.data().get(mime).toString().contains("""
                <th style="text-align: center;">Key</th><th style="text-align: center;">Value</th></tr>
                </thead>
                <tbody>"""
        ));

    }
}
