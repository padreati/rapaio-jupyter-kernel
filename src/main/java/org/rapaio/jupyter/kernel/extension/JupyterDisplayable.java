package org.rapaio.jupyter.kernel.extension;

import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.MIMEType;

/**
 * Interface which allows any object to be rendered in a display data object.
 */
public interface JupyterDisplayable {

    MIMEType defaultMIMEType();

    /**
     * Produces a display data object with the default MIME type. The default
     * MIME type is determined by {@link #defaultMIMEType()}.
     *
     * @return display data object of default type
     */
    default DisplayData render() {
        return render(defaultMIMEType());
    }

    /**
     * Produces a display data object of given MIME type. If the `mimeType` parameter is null
     * or if the given value is not recognized by the implementation, the used type is
     * the default one.
     *
     * @param mimeType MIME type of the display object; if null or unrecognized, the default value is used
     * @return display data object with given type
     */
    DisplayData render(MIMEType mimeType);
}
