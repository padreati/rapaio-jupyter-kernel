package org.rapaio.jupyter.kernel.display.table;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DataTypeTest {

    @Test
    void testIntegerValues() {
        assertEquals("", DataType.INTEGER.format(null));
        assertEquals("", DataType.INTEGER.format(""));
        assertEquals("-23", DataType.INTEGER.format("-23"));
        assertEquals("Ana23", DataType.INTEGER.format("Ana23"));
    }

    @Test
    void testFloatValues() {
        assertEquals("", DataType.FLOAT.format(null));
        assertEquals("", DataType.FLOAT.format(""));
        assertEquals("-23.1", DataType.FLOAT.format("-23.1"));
        assertEquals("Ana-23.1", DataType.FLOAT.format("Ana-23.1"));
    }

    @Test
    void testStringValues() {
        assertEquals("", DataType.STRING.format(null));
        assertEquals("", DataType.STRING.format(""));
        assertEquals("Ana\"23\"", DataType.STRING.format("Ana\"23\""));
        assertEquals("a".repeat(50)+"...", DataType.STRING.format("a".repeat(10000)));
    }
}
