package org.rapaio.jupyter.kernel.display;

/**
 * Renders objects to {@link DisplayData} if the objects can be cast to the class specified by {@link #rendererClass()}
 * and the requested MIME type is supported by the renderer.
 * <p>
 * A supported MIME type is one for which {@link #canRender(String)} returns `true`.
 * <p>
 * The default renderer is {@link DefaultDisplayRenderer} which renders all objects to
 * a string representation.
 * <p>
 * Implementations of this interface could be provided through SPI services by providing implementations
 * of {@link org.rapaio.jupyter.kernel.display.spi.DisplayRendererProvider}.
 *
 * @see org.rapaio.jupyter.kernel.display.spi.DisplayRendererProvider
 */
public interface DisplayRenderer {

    /**
     * Parent class of the objects which can be rendered by this renderer.
     *
     * @return parent class of the objects which can be rendered
     */
    Class<?> rendererClass();

    /**
     * Checks if the given MIME type is supported by this renderer.
     *
     * @param mime MIME type to be checked
     * @return true if the MIME type is supported, false otherwise
     */
    boolean canRender(String mime);

    /**
     * Renders the given object to a {@link DisplayData} object with a given MIME type.
     *
     * @param mime MIME type to be checked
     * @param o    object to be rendered
     * @return {@link DisplayData} object which can be rendered by a renderer
     */
    DisplayData render(String mime, Object o);
}
