package org.rapaio.jupyter.kernel.display.table;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.rapaio.jupyter.kernel.global.Global;

public class TableDisplayWrapper implements TableDisplay {

    private final List<String> columnNames = new ArrayList<>();
    private final List<DataType> dataTypes = new ArrayList<>();
    private final List<Function<Integer, Object>> columnGetters = new ArrayList<>();

    private int rows;

    @Override
    public boolean hasHeader() {
        return columnNames.stream().anyMatch(String::isBlank);
    }

    @Override
    public String columnName(int col) {
        return columnNames.get(col);
    }

    @Override
    public DataType columnType(int col) {
        return dataTypes.get(col);
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getCols() {
        return columnNames.size();
    }

    @Override
    public String getValue(int row, int col) {
        var value = columnGetters.get(col).apply(row);
        return value == null ? Global.options().display().format().na() : value.toString();
    }

    public TableDisplayWrapper withColumn(String name, DataType type, List<Object> values) {
        columnNames.add(name);
        dataTypes.add(type);
        columnGetters.add(values::get);
        rows = Math.max(rows, values.size());
        return this;
    }

    public TableDisplayWrapper withColumn(String name, DataType type, int size, Function<Integer, Object> getter) {
        columnNames.add(name);
        dataTypes.add(type);
        columnGetters.add(getter);
        rows = Math.max(rows, size);
        return this;
    }
}
