package org.rapaio.jupyter.kernel.display.spi;

import java.util.List;

import org.rapaio.jupyter.kernel.display.DisplayTransformer;

public interface DisplayTransformerProvider {

    List<DisplayTransformer> getDisplayTransformers();
}
