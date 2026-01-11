package org.rapaio.jupyter.display.provider.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.display.table.TableDisplay;

public class ExampleMapTableTransformerTest {

    private static final ExampleMapTableTransformer transformer = new ExampleMapTableTransformer();

    @Test
    void testCanTransform() {
        // test that the transformer can transform a map
        assert transformer.canTransform(Map.of());
    }

    @Test
    void testTransform() {
        // test that the transformer transforms a map into a table

        Map<String, Integer> m = new ConcurrentHashMap<>();
        m.put("a", 1);
        m.put("b", 2);

        var table = transformer.transform(m);
        assertInstanceOf(TableDisplay.class, table);

        var td = (TableDisplay) table;
        assertEquals(2, td.getRows());
        assertEquals(2, td.getCols());
        assertEquals("Key", td.columnName(0));
        assertEquals("Value", td.columnName(1));
        assertEquals("a", td.getValue(0, 0));
        assertEquals("1", td.getValue(0, 1));
        assertEquals("b", td.getValue(1, 0));
        assertEquals("2", td.getValue(1, 1));
    }
}
