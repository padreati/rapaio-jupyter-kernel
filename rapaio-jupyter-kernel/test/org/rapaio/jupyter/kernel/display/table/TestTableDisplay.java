package org.rapaio.jupyter.kernel.display.table;

class TestTableDisplay implements TableDisplay {

    private final int rows;
    private final int cols;
    private final boolean hasHeader;

    private final String[][] data;

    public TestTableDisplay(int rows, int cols, boolean hasHeader) {
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
