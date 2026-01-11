package org.rapaio.jupyter.kernel.display.table;

public interface TableDisplay {

    enum Alignment {
        LEFT("left"),
        RIGHT("right"),
        CENTER("center");

        private final String html;

        private Alignment(String html) {
            this.html = html;
        }

        public String html() {
            return html;
        }
    }

    boolean hasHeader();

    int getRows();

    int getCols();

    default Alignment columnAlignment(int col) {
        return switch (columnType(col)) {
            case INTEGER, FLOAT -> Alignment.RIGHT;
            default -> Alignment.LEFT;
        };
    }

    String columnName(int col);

    DataType columnType(int col);

    Object getValue(int row, int col);

    default String getFormattedValue(int row, int col) {
        return columnType(col).format(getValue(row, col));
    }
}
