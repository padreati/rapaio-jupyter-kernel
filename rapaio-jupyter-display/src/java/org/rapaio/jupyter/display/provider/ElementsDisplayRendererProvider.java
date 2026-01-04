package org.rapaio.jupyter.display.provider;

import java.util.List;

import org.rapaio.jupyter.display.provider.list.ExampleListDisplayRenderer;
import org.rapaio.jupyter.display.provider.list.ExampleSetDisplayRenderer;
import org.rapaio.jupyter.kernel.display.DisplayRenderer;
import org.rapaio.jupyter.kernel.display.spi.DisplayRendererProvider;

public class ElementsDisplayRendererProvider implements DisplayRendererProvider {

    @Override
    public List<DisplayRenderer> getDisplayRenderers() {
        return List.of(
                new ExampleListDisplayRenderer(),
                new ExampleSetDisplayRenderer()
        );
    }
}
