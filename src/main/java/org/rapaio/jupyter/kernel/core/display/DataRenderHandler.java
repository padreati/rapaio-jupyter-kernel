package org.rapaio.jupyter.kernel.core.display;

/**
 * Interface which describes a facility which is able to render
 * objects for notebook output.
 */
public interface DataRenderHandler {

    boolean canRender(Object o);

    MIMEType defaultMIMEType();

    default DisplayData render(Object o) {
        return render(defaultMIMEType(), o);
    }

    DisplayData render(MIMEType mimeType, Object o);
}
