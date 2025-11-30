package org.rapaio.jupyter.kernel.display;

/**
 * Interface which allows any object to be rendered in a display data object.
 */
public interface Displayable {

    String defaultMIME();

    /**
     * Produces a display data object with the default MIME type. The default
     * MIME type is determined by {@link #defaultMIME()}.
     *
     * @return display data object of default type
     */
    default DisplayData render() {
        return render(defaultMIME());
    }

    /**
     * Produces a display data object of given MIME type. If the `mime` parameter is null
     * or if the given value is not recognized by the implementation, the used type is
     * the default one.
     *
     * @param mime MIME type of the display object; if null or unrecognized, the default value is used
     * @return display data object with given type
     */
    DisplayData render(String mime);
}
