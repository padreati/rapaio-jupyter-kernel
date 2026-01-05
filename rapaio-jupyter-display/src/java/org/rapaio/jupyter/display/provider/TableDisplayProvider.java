package org.rapaio.jupyter.display.provider;

import java.util.List;

import org.rapaio.jupyter.display.provider.table.ExampleMapTableTransformer;
import org.rapaio.jupyter.kernel.display.DisplayRenderer;
import org.rapaio.jupyter.kernel.display.DisplayTransformer;
import org.rapaio.jupyter.kernel.display.spi.DisplayProvider;

public class TableDisplayProvider implements DisplayProvider {

    @Override
    public List<DisplayRenderer> getDisplayRenderers() {
        return List.of();
    }

    @Override
    public List<DisplayTransformer> getDisplayTransformers() {
        return List.of(new ExampleMapTableTransformer());
    }
}
