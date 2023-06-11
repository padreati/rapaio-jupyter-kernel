package org.rapaio.jupyter.kernel.core.display;

import org.rapaio.jupyter.kernel.core.display.image.ImageRenderer;
import org.rapaio.jupyter.kernel.extension.JupyterDisplayable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Renderer {

    private static final List<DataRenderHandler> handlers = new ArrayList<>();

    private static final DataRenderHandler backoffHandler = new DataRenderHandler() {

        @Override
        public boolean canRender(Object o) {
            return true;
        }

        @Override
        public MIMEType defaultMIMEType() {
            return MIMEType.TEXT;
        }

        @Override
        public DisplayData render(MIMEType mimeType, Object o) {
            if (o == null) {
                return null;
            }
            return DisplayData.withType(mimeType, o.toString());
        }
    };

    static {
        handlers.add(new ImageRenderer());
    }

    public DisplayData render(Object o) {
        return render(null, o);
    }

    public DisplayData render(MIMEType mimeType, Object o) {
        if(o instanceof JupyterDisplayable displayable) {
            MIMEType callMimeType = mimeType == null ? displayable.defaultMIMEType() : mimeType;
            return withId(displayable.render(callMimeType));
        }
        for (var handler : handlers) {
            if (handler.canRender(o)) {
                MIMEType callMimeType = mimeType == null ? handler.defaultMIMEType() : mimeType;
                return withId(handler.render(callMimeType, o));
            }
        }
        MIMEType callMimeType = mimeType == null ? backoffHandler.defaultMIMEType() : mimeType;
        return withId(backoffHandler.render(callMimeType, o));
    }

    private DisplayData withId(DisplayData displayData) {
        if(!displayData.hasDisplayId()) {
            displayData.setDisplayId(UUID.randomUUID().toString());
        }
        return displayData;
    }
}
