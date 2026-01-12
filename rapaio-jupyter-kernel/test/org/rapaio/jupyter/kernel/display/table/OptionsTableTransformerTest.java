package org.rapaio.jupyter.kernel.display.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.global.Global;

public class OptionsTableTransformerTest {

    @Test
    void testCanTransform() {
        OptionsTableTransformer transformer = new OptionsTableTransformer();
        assertFalse(transformer.canTransform(null));
        assertTrue(transformer.canTransform(Global.options()));
    }

    @Test
    void testTransformedClass() {
        OptionsTableTransformer transformer = new OptionsTableTransformer();
        assertTrue(transformer.transformedClass().isAssignableFrom(TableDisplay.class));
    }

    @Test
    void testTransform() {
        OptionsTableTransformer transformer = new OptionsTableTransformer();

        var display = transformer.transform(Global.options());
        assertInstanceOf(TableDisplay.class, display);

        TableDisplay td = (TableDisplay) display;
        assertEquals(3, td.getCols());
        assertEquals(11, td.getRows());
        assertTrue(td.hasHeader());
    }
}
