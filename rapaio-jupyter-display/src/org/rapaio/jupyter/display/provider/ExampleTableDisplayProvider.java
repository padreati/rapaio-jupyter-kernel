package org.rapaio.jupyter.display.provider;

import java.util.List;

import org.rapaio.jupyter.display.provider.table.ExampleMapTableTransformer;
import org.rapaio.jupyter.kernel.display.DisplayRenderer;
import org.rapaio.jupyter.kernel.display.DisplayTransformer;
import org.rapaio.jupyter.kernel.display.spi.DisplayProvider;

/**
 * Example display provider, which provides display transformers for
 * maps as tables.
 * <p>
 * This is used to demonstrate how to create a display provider.
 */
public class ExampleTableDisplayProvider implements DisplayProvider {

    @Override
    public List<DisplayRenderer> getDisplayRenderers() {
        return List.of();
    }

    @Override
    public List<DisplayTransformer> getDisplayTransformers() {
        return List.of(new ExampleMapTableTransformer());
    }
}
