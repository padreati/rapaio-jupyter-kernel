package org.rapaio.jupyter.kernel.display.spi;

import java.util.List;

import org.rapaio.jupyter.kernel.display.DisplayRenderer;

/**
 * Service Provider Interface for {@link DisplayRenderer} instances.
 */
public interface DisplayRendererProvider {

    List<DisplayRenderer> getDisplayRenderers();
}
