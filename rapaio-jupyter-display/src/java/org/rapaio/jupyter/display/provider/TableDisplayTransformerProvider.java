package org.rapaio.jupyter.display.provider;

import java.util.List;

import org.rapaio.jupyter.display.provider.table.MapTableTransformer;
import org.rapaio.jupyter.kernel.display.DisplayTransformer;
import org.rapaio.jupyter.kernel.display.spi.DisplayTransformerProvider;

public class TableDisplayTransformerProvider implements DisplayTransformerProvider {

    @Override
    public List<DisplayTransformer> getDisplayTransformers() {
        return List.of(new MapTableTransformer());
    }
}
