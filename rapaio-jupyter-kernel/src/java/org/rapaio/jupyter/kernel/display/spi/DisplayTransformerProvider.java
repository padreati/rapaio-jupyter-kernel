package org.rapaio.jupyter.kernel.display.spi;

import java.util.List;

import org.rapaio.jupyter.kernel.display.DisplayTransformer;

/**
 * Service Provider Interface for {@link DisplayTransformer} instances.
 */
public interface DisplayTransformerProvider {

    List<DisplayTransformer> getDisplayTransformers();
}
