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
        return new TableDisplayWrapper()
                .withColumn("Name", DataType.STRING, opt.getConfigItems().size(), i -> opt.getConfigItems().get(i).key())
                .withColumn("Value", DataType.STRING, opt.getConfigItems().size(), i -> opt.getConfigItems().get(i).value())
                .withColumn("Description", DataType.STRING, opt.getConfigItems().size(), i -> opt.getConfigItems().get(i).description());
    }
}
