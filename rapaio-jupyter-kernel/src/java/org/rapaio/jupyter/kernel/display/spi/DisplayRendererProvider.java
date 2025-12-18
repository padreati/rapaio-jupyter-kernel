package org.rapaio.jupyter.kernel.display.spi;

import java.util.List;

import org.rapaio.jupyter.kernel.display.DisplayRenderer;

public interface DisplayRendererProvider {

    List<DisplayRenderer> getDisplayRenderers();
}
