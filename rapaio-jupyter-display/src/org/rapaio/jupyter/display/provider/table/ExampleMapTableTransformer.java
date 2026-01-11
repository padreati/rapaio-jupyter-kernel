package org.rapaio.jupyter.display.provider.table;

import java.util.Map;

import org.rapaio.jupyter.kernel.display.DisplayTransformer;
import org.rapaio.jupyter.kernel.display.table.DataType;
import org.rapaio.jupyter.kernel.display.table.TableDisplay;
import org.rapaio.jupyter.kernel.display.table.TableDisplayWrapper;

public class ExampleMapTableTransformer implements DisplayTransformer {


    @Override
    public boolean canTransform(Object o) {
        return o instanceof Map<?, ?>;
    }

    @Override
    public Class<?> transformedClass() {
        return TableDisplay.class;
    }

    @Override
    public Object transform(Object o) {
        Map<?,?> map = (Map<?,?>) o;
        var entries = map.entrySet().toArray(new Map.Entry[0]);
        return new TableDisplayWrapper()
                .withColumn("Key", DataType.STRING, map.size(), i -> String.valueOf(entries[i].getKey()))
                .withColumn("Value", DataType.STRING, map.size(), i -> String.valueOf(entries[i].getValue()));
    }
}
