package org.rapaio.jupyter.display.provider;

import java.util.List;

import org.rapaio.jupyter.kernel.display.DisplayRenderer;
import org.rapaio.jupyter.kernel.display.spi.DisplayRendererProvider;

public class TableDisplayRendererProvider implements DisplayRendererProvider {

    @Override
    public List<DisplayRenderer> getDisplayRenderers() {
        return List.of();
    }
}
