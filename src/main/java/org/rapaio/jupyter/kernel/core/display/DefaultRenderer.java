package org.rapaio.jupyter.kernel.core.display;

import java.util.ArrayList;
import java.util.List;

import org.rapaio.jupyter.kernel.core.display.image.ImageHandler;

public class DefaultRenderer implements Renderer {

    private static final List<DataRenderHandler> handlers = new ArrayList<>();

    private static final DataRenderHandler backoffHandler =
            (o, args) -> o == null ? null : new DataRendererResponse(true, DisplayData.withText(o.toString()));

    static {
        handlers.add(new ImageHandler());
    }

    @Override
    public DisplayData render(Object result, String... args) {
        for (var handler : handlers) {
            var response = handler.render(result, args);
            if (response.handled()) {
                return response.displayData();
            }
        }
        return backoffHandler.render(result).displayData();
    }

}
