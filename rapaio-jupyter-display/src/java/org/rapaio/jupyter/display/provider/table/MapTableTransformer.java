package org.rapaio.jupyter.display.provider.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rapaio.jupyter.kernel.display.DisplayTransformer;
import org.rapaio.jupyter.kernel.display.table.TableDisplayModel;

public class MapTableTransformer implements DisplayTransformer {


    @Override
    public Class<TableDisplayModel> transformerClass() {
        return TableDisplayModel.class;
    }

    @Override
    public boolean canTransform(Object o) {
        if (o == null) {
            return false;
        }
        return o.getClass().isAssignableFrom(Map.class);
    }

    @Override
    public TableDisplayModel transform(Object o) {
        return new MapTableModel((Map<?, ?>) o);
    }

    static class MapTableModel implements TableDisplayModel {

        private static final int MAX = 20;
        private final List<Object> keys = new ArrayList<>();
        private final List<Object> values = new ArrayList<>();

        public MapTableModel(Map<?, ?> map) {
            int len = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                keys.add(entry.getKey());
                values.add(entry.getValue());
                len++;
                if (len == MAX) {
                    keys.add("...");
                    values.add("...");
                    break;
                }
            }
        }

        @Override
        public int getCols() {
            return 2;
        }

        @Override
        public int getRows() {
            return keys.size() + headerRows();
        }

        @Override
        public int headerRows() {
            return 1;
        }

        @Override
        public String getValue(int row, int col) {
            if (row == 0) {
                return col == 0 ? "Key" : "Value";
            }
            if (col == 0) {
                Object key = keys.get(row - 1);
                key = key == null ? "null" : key;
                return key.toString();
            } else {
                Object value = values.get(row - 1);
                value = value == null ? "null" : value;
                return value.toString();
            }
        }
    }
}