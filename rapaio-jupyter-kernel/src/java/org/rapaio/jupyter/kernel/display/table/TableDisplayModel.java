package org.rapaio.jupyter.kernel.display.table;

/**
 * Model used to display an object as a table in Jupyter.
 * <p>
 * The easiest way to make an object displayable as a table is to implement a SPI
 * for {@link org.rapaio.jupyter.kernel.display.DisplayTransformer} which transforms
 * your objects into {@link TableDisplayModel} instances. Then, the default renderer
 * will handle and display your objects as tables.
 */
public interface TableDisplayModel {

    /**
     * Number of rows from the table. It includes the header rows.
     * @return total number of rows
     */
    int getRows();

    /**
     * Number of header rows.
     * @return number of header rows
     */
    int headerRows();

    /**
     * Number of columns in the table.
     * @return number of columns
     */
    int getCols();

    /**
     * Get the value of a cell in the table.
     * @param row row index
     * @param col column index
     * @return value of the cell
     */
    String getValue(int row, int col);
}
