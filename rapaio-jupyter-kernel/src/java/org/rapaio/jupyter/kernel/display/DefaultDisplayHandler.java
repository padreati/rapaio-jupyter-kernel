package org.rapaio.jupyter.kernel.display;

/**
 * Implements a generic display handler for all kins of objects.
 * This handler acts as a back-off handler, meaning that it will be used
 * only if no other handler is found for the object to be rendered.
 * <p>
 * The default handler displays the string representation of the object.
 */
public class DefaultDisplayHandler implements DisplayHandler {

    @Override
    public boolean canRender(Object o) {
        return true;
    }

    @Override
    public String defaultMIMEType() {
        return MimeType.TEXT.toString();
    }

    @Override
    public DisplayData render(String mimeType, Object o) {
        if (o == null) {
            return null;
        }
        return DisplayData.withType(mimeType == null ? defaultMIMEType() : mimeType, o.toString());
    }
}
