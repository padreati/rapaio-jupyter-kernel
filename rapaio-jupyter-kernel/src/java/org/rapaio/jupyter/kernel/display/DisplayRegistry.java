package org.rapaio.jupyter.kernel.display;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.rapaio.jupyter.kernel.display.image.ImageDisplayHandler;
import org.rapaio.jupyter.kernel.display.table.TableDisplayHandler;

public final class DisplayRegistry {

    private static final List<DisplayHandler> displayHandlers = new ArrayList<>();
    private static final DefaultDisplayHandler defaultDisplayHandler = new DefaultDisplayHandler();

    static {
        displayHandlers.add(new ImageDisplayHandler());
        displayHandlers.add(new TableDisplayHandler());
    }

    public DisplayData render(Object o) {
        return render(null, o);
    }

    /**
     * Produces the display data which will be passed to the notebook cell's output
     * from an object and an optional mime type.
     *
     * @param mimeType
     * @param o
     * @return
     */
    public DisplayData render(String mimeType, Object o) {
        if (o instanceof Displayable displayable) {
            String callMimeType = mimeType == null ? displayable.defaultMIME() : mimeType;
            return withId(displayable.render(callMimeType));
        }
        for (var handler : displayHandlers) {
            if (handler.canRender(o)) {
                String callMimeType = mimeType == null ? handler.defaultMIMEType() : mimeType;
                return withId(handler.render(callMimeType, o));
            }
        }
        String callMimeType = mimeType == null ? defaultDisplayHandler.defaultMIMEType() : mimeType;
        return withId(defaultDisplayHandler.render(callMimeType, o));
    }

    private DisplayData withId(DisplayData displayData) {
        if (!displayData.hasDisplayId()) {
            displayData.setDisplayId(UUID.randomUUID().toString());
        }
        return displayData;
    }
}
