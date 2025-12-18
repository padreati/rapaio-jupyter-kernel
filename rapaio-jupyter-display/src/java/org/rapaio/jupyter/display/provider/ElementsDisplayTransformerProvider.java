package org.rapaio.jupyter.display.provider;

import java.util.List;

import org.rapaio.jupyter.kernel.display.DisplayTransformer;
import org.rapaio.jupyter.kernel.display.spi.DisplayTransformerProvider;

public class ElementsDisplayTransformerProvider implements DisplayTransformerProvider {

    @Override
    public List<DisplayTransformer> getDisplayTransformers() {
        return List.of();
    }
}
