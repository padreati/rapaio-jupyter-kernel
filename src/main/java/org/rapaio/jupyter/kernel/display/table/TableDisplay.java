package org.rapaio.jupyter.kernel.display.table;

public interface TableDisplay {

    int getCols();

    int getRows();

    int headerRows();

    String getValue(int row, int col);
}
