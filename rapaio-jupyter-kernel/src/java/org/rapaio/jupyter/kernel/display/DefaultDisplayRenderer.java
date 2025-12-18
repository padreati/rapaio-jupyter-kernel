package org.rapaio.jupyter.kernel.display;

import org.rapaio.jupyter.kernel.global.Global;

/**
 * Implements a generic display handler for all kins of objects.
 * This handler acts as a back-off handler, meaning that it will be used
 * only if no other handler is found for the object to be rendered.
 * <p>
 * The default handler displays the string representation of the object.
 */
public class DefaultDisplayRenderer implements DisplayRenderer {

    @Override
    public Class<?> rendererClass() {
        return Object.class;
    }

    @Override
    public boolean canRender(String mime) {
        return true;
    }

    @Override
    public DisplayData render(String mime, Object o) {
        MimeType mimeType = MimeType.from(mime, Global.config().display().defaultMime());
        return DisplayData.withType(mimeType.longType(), (o == null) ? "null" : o.toString());
    }
}
