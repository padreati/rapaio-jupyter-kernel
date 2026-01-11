package org.rapaio.jupyter.kernel.display.spi;

import java.util.List;

import org.rapaio.jupyter.kernel.display.DisplayRenderer;
import org.rapaio.jupyter.kernel.display.DisplayTransformer;

/**
 * Service Provider Interface for {@link DisplayRenderer} and {@link DisplayTransformer} instances.
 */
public interface DisplayProvider {

    List<DisplayRenderer> getDisplayRenderers();

    List<DisplayTransformer> getDisplayTransformers();
}
