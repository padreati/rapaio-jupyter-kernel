package org.rapaio.jupyter.display.provider.table;

import java.util.Map;

import org.rapaio.jupyter.kernel.display.DisplayTransformer;
import org.rapaio.jupyter.kernel.display.table.DataType;
import org.rapaio.jupyter.kernel.display.table.TableDisplay;
import org.rapaio.jupyter.kernel.display.table.TableDisplayWrapper;

/**
 * Example display transformer, which transforms a map into a table.
 * <p>
 * The map instance information is transformed into a table with two columns: key and value.
 * The rows from the table are equal to the size of the map and the order of the keys is the
 * one provided by the map implementation.
 */
public class ExampleMapTableTransformer implements DisplayTransformer {


    @Override
    public boolean canTransform(Object o) {
        // true if the object is a map
        return o instanceof Map<?, ?>;
    }

    @Override
    public Class<?> transformedClass() {
        // maps are transformed into Table Display
        return TableDisplay.class;
    }

    @Override
    public Object transform(Object o) {
        if(!canTransform(o)) {
            throw new IllegalArgumentException("Cannot transform object of type: " + o.getClass().getName());
        }
        Map<?,?> map = (Map<?,?>) o;
        var entries = map.entrySet().toArray(new Map.Entry[0]);
        // we use a table display wrapper which is an easy way to wrap something as a table display model
        return new TableDisplayWrapper()
                .withColumn("Key", DataType.STRING, map.size(), i -> String.valueOf(entries[i].getKey()))
                .withColumn("Value", DataType.STRING, map.size(), i -> String.valueOf(entries[i].getValue()));
    }
}
