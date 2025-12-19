package org.rapaio.jupyter.kernel.display;

/**
 * Handler for rendering objects to {@link DisplayData}. Implementations of this interface
 * should be able to render objects of a given class specified bu {@link #rendererClass()},
 * the supported MIME types returns `true` for {@link #canRender(String)}.
 * <p>
 * The default handler is {@link DefaultDisplayRenderer} which renders all objects to
 * a string representation.
 * <p>
 * Implementations of this interface could be provided through SPI services.
 *
 * @see org.rapaio.jupyter.kernel.display.spi.DisplayRendererProvider
 */
public interface DisplayRenderer {

    /**
     * Parent class of the objects which can be rendered by this handler.
     *
     * @return parent class of the objects which can be rendered by this handler
     */
    Class<?> rendererClass();

    /**
     * Checks if the given MIME type is supported by this handler.
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
