package org.rapaio.jupyter.display.provider;

import java.util.List;

import org.rapaio.jupyter.display.provider.elements.ExampleListDisplayRenderer;
import org.rapaio.jupyter.display.provider.elements.ExampleSetDisplayRenderer;
import org.rapaio.jupyter.kernel.display.DisplayRenderer;
import org.rapaio.jupyter.kernel.display.DisplayTransformer;
import org.rapaio.jupyter.kernel.display.spi.DisplayProvider;

/**
 * Example display provider, which provides display renderers and transformers for
 * lists and sets.
 * <p>
 * This is used to demonstrate how to create a display provider.
 */
public class ExampleElementsDisplayProvider implements DisplayProvider {

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
