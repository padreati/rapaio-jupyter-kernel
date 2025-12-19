package org.rapaio.jupyter.display.provider.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rapaio.jupyter.kernel.display.DisplayTransformer;
import org.rapaio.jupyter.kernel.display.table.TableDisplayModel;
import org.rapaio.jupyter.kernel.global.Global;

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
        return Map.class.isAssignableFrom(o.getClass());
    }

    @Override
    public TableDisplayModel transform(Object o) {
        return new MapTableModel((Map<?, ?>) o);
    }

    static class MapTableModel implements TableDisplayModel {

        private final List<Object> keys = new ArrayList<>();
        private final List<Object> values = new ArrayList<>();

        public MapTableModel(Map<?, ?> map) {
            if(map.isEmpty()) {
                return;
            }
            keys.add("Key");
            values.add("Value");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                keys.add(entry.getKey());
                values.add(entry.getValue());
                if (keys.size() == Global.config().display().maxRows() + 2) {
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
            return keys.size();
        }

        @Override
        public int headerRows() {
            return Math.min(keys.size(), 1);
        }

        @Override
        public String getValue(int row, int col) {
            if (col == 0) {
                Object key = keys.get(row);
                return key == null ? "null" : key.toString();
            } else {
                Object value = values.get(row);
                return value == null ? "null" : value.toString();
            }
        }
    }
}