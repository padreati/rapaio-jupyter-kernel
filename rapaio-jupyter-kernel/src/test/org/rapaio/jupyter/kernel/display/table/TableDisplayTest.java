package org.rapaio.jupyter.kernel.display.table;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TableDisplayTest {

    private final DefaultTableDisplayRenderer renderer = new DefaultTableDisplayRenderer();

    @Test
    void testCanRender() {
        TableDisplayModel td = new TableDisplayModel(2, 2, true);
        assertTrue(renderer.canRender(null));
    }
/*
    @Test
    void testDefaultMIMEType() {
        assertEquals(MIMEType.HTML.toString(), handler.defaultMIMEType());
    }

    @Test
    void testRenderHtml() {
        TableDisplay td = new TableDisplay(2, 2, 1);
        td.setValue(0, 0, "Header1");
        td.setValue(0, 1, "Header2");
        td.setValue(1, 0, "Data1");
        td.setValue(1, 1, "Data2");

        DisplayData result = handler.render(MIMEType.HTML.toString(), td);

        assertNotNull(result);
        assertNotNull(result.getDisplayId());
        String html = result.getData().get(MIMEType.HTML.toString()).toString();

        assertTrue(html.contains("<style>"));
        assertTrue(html.contains("border-collapse: collapse"));
        assertTrue(html.contains("<table id="));
        assertTrue(html.contains("<thead>"));
        assertTrue(html.contains("<tbody>"));
        assertTrue(html.contains("Header1"));
        assertTrue(html.contains("Data1"));
    }

    @Test
    void testRenderMarkdown() {
        TableDisplay td = new TableDisplay(2, 2, 1);
        td.setValue(0, 0, "Header1");
        td.setValue(0, 1, "Header2");
        td.setValue(1, 0, "Data1");
        td.setValue(1, 1, "Data2");

        DisplayData result = handler.render(MIMEType.MARKDOWN.toString(), td);

        assertNotNull(result);
        String markdown = result.getData().get(MIMEType.MARKDOWN.toString()).toString();

        assertTrue(markdown.contains("| Header1 |Header2 |"));
        assertTrue(markdown.contains("| --- |--- |"));
        assertTrue(markdown.contains("| Data1 |Data2 |"));
    }

    @Test
    void testRenderText() {
        TableDisplay td = new TableDisplay(2, 2, 1);
        DisplayData result = handler.render(MIMEType.TEXT.toString(), td);

        assertNotNull(result);
        assertEquals("Not implemented yet.", result.getData().get(MIMEType.TEXT.toString()));
    }

    @Test
    void testRenderWithNullMimeType() {
        TableDisplay td = new TableDisplay(1, 1, 0);
        DisplayData result = handler.render(null, td);

        assertNotNull(result);
        assertTrue(result.getData().containsKey(MIMEType.HTML.toString()));
    }

    @Test
    void testRenderWithInvalidObject() {
        DisplayData result = handler.render(MIMEType.HTML.toString(), "not a table");
        assertNull(result);
    }

 */
}

class TableDisplayModel implements TableDisplay {

    private final int rows;
    private final int cols;
    private final boolean hasHeader;

    private final String[][] data;

    public TableDisplayModel(int rows, int cols, boolean hasHeader) {
        this.rows = rows;
        this.cols = cols;
        this.hasHeader = hasHeader;
        this.data = new String[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = "D[" + i + "," + j + "]";
            }
        }
    }

    @Override
    public int getCols() {
        return cols;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public boolean hasHeader() {
        return hasHeader;
    }

    @Override
    public String columnName(int col) {
        return "Column[" + col + "]";
    }

    @Override
    public DataType columnType(int col) {
        return DataType.STRING;
    }

    @Override
    public String getValue(int row, int col) {
        return data[row][col];
    }
}