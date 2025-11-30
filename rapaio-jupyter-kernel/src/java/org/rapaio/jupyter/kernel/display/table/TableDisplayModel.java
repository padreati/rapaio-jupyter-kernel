package org.rapaio.jupyter.kernel.display.table;

public interface TableDisplayModel {

    int getCols();

    int getRows();

    int headerRows();

    String getValue(int row, int col);
}
