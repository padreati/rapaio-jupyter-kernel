package org.rapaio.jupyter.display.provider.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.display.DisplaySystem;
import org.rapaio.jupyter.kernel.display.table.TableDisplayModel;
import org.rapaio.jupyter.kernel.global.Global;

public class MapTableTransformerTest {

    private final  MapTableTransformer transformer = new MapTableTransformer();

    @Test
    void testClass() {
        assertEquals(TableDisplayModel.class, transformer.transformerClass());
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
        assertEquals(0, model.headerRows());
    }

    @Test
    void testSmallMap() {
        SortedMap<String, Integer> map = new TreeMap<>(Map.of("a", 1, "b", 2));
        var model = transformer.transform(map);
        assertEquals(3, model.getRows());
        assertEquals(2, model.getCols());
        assertEquals(1, model.headerRows());

        assertEquals("Key", model.getValue(0, 0));
        assertEquals("a", model.getValue(1, 0));
        assertEquals("b", model.getValue(2, 0));
        assertEquals("Value", model.getValue(0, 1));
        assertEquals("1", model.getValue(1, 1));
        assertEquals("2", model.getValue(2, 1));
    }

    @Test
    void mapTransformerPipeline() {
        DisplaySystem.inst().refreshSpiDisplayHandlers();
        var dd = DisplaySystem.inst().render(new TreeMap<>(Map.of("a", 1, "b", 2)));

        String mime = Global.config().display().defaultMime();
        assertTrue(dd.data().containsKey(mime));
        assertTrue(dd.data().get(mime).toString().contains("""
                <th>Key</th><th>Value</th></tr>
                </thead>
                <tbody>"""
        ));

    }
}
