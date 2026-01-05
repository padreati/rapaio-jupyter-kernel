package org.rapaio.jupyter.display.provider;

import java.util.List;

import org.rapaio.jupyter.display.provider.list.ExampleListDisplayRenderer;
import org.rapaio.jupyter.display.provider.list.ExampleSetDisplayRenderer;
import org.rapaio.jupyter.kernel.display.DisplayRenderer;
import org.rapaio.jupyter.kernel.display.DisplayTransformer;
import org.rapaio.jupyter.kernel.display.spi.DisplayProvider;

public class ElementsDisplayProvider implements DisplayProvider {

    @Override
    public List<DisplayRenderer> getDisplayRenderers() {
        return List.of(
                new ExampleListDisplayRenderer(),
                new ExampleSetDisplayRenderer()
        );
    }

    @Override
    public List<DisplayTransformer> getDisplayTransformers() {
        return List.of();
    }
}
