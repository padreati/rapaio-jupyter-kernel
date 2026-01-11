package org.rapaio.jupyter.kernel.display.table;

import org.rapaio.jupyter.kernel.display.DisplayTransformer;
import org.rapaio.jupyter.kernel.global.Options;

public class OptionsTableTransformer implements DisplayTransformer {

    @Override
    public boolean canTransform(Object o) {
        return o instanceof Options;
    }

    @Override
    public Class<?> transformedClass() {
        return TableDisplay.class;
    }

    @Override
    public Object transform(Object o) {
        Options opt = (Options) o;
        int size = opt.getConfigItems().size();
        return new TableDisplayWrapper()
                .withColumn("Name", DataType.STRING, size, i -> opt.getConfigItems().get(i).key())
                .withColumn("Value", DataType.STRING, size, i -> opt.getConfigItems().get(i).value())
                .withColumn("Description", DataType.STRING, size, i -> opt.getConfigItems().get(i).description());
    }
}
