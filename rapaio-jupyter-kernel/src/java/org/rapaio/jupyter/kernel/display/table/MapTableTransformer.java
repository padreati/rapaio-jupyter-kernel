package org.rapaio.jupyter.kernel.display.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rapaio.jupyter.kernel.display.DisplayTransformer;
import org.rapaio.jupyter.kernel.global.Global;

public class MapTableTransformer implements DisplayTransformer {


    @Override
    public Class<TableDisplay> transformedClass() {
        return TableDisplay.class;
    }

    @Override
    public boolean canTransform(Object o) {
        if (o == null) {
            return false;
        }
        return Map.class.isAssignableFrom(o.getClass());
    }

    @Override
    public TableDisplay transform(Object o) {
        return new MapTableDisplay((Map<?, ?>) o);
    }

    static class MapTableDisplay implements TableDisplay {

        private final DataType[] types = new DataType[2];
        private final List<Object> keys = new ArrayList<>();
        private final List<Object> values = new ArrayList<>();

        public MapTableDisplay(Map<?, ?> map) {
            if (map.isEmpty()) {
                return;
            }
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (types[0] == null && entry.getKey() != null) {
                    types[0] = DataType.detectType(entry.getKey());
                }
                keys.add(entry.getKey());
                if (types[1] == null && entry.getValue() != null) {
                    types[1] = DataType.detectType(entry.getValue());
                }
                values.add(entry.getValue());
            }
            for (int i = 0; i < types.length; i++) {
                if (types[i] == null) {
                    types[i] = DataType.STRING;
                }
            }
        }

        @Override
        public int getCols() {
            return 2;
        }

        @Override
        public int getRows() {
            return keys.size();
        }

        @Override
        public boolean hasHeader() {
            return true;
        }

        @Override
        public String columnName(int col) {
            return col == 0 ? "Key" : "Value";
        }

        @Override
        public DataType columnType(int col) {
            return types[col] != null ? types[col] : DataType.STRING;
        }

        @Override
        public String getValue(int row, int col) {
            if (col == 0) {
                Object key = keys.get(row);
                return key == null ? Global.options().display().format().na() : key.toString();
            } else {
                Object value = values.get(row);
                return value == null ? Global.options().display().format().na() : value.toString();
            }
        }
    }
}