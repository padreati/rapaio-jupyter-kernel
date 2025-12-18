package org.rapaio.jupyter.display.provider;

import java.util.List;

import org.rapaio.jupyter.display.provider.list.ListDisplayRenderer;
import org.rapaio.jupyter.display.provider.list.SetDisplayRenderer;
import org.rapaio.jupyter.kernel.display.DisplayRenderer;
import org.rapaio.jupyter.kernel.display.spi.DisplayRendererProvider;

public class ElementsDisplayRendererProvider implements DisplayRendererProvider {

    @Override
    public List<DisplayRenderer> getDisplayRenderers() {
        return List.of(
                new ListDisplayRenderer(),
                new SetDisplayRenderer()
        );
    }
}
