package org.rapaio.jupyter.kernel.display;

/**
 * Interface which describes a facility which is able to render
 * objects for notebook output.
 */
public interface DisplayHandler {

    boolean canRender(Object o);

    String defaultMIMEType();

    default DisplayData render(Object o) {
        return render(defaultMIMEType(), o);
    }

    DisplayData render(String mimeType, Object o);
}
