package org.rapaio.jupyter.kernel.display;

/**
 * Handler for rendering objects to {@link DisplayData}.
 */
public interface DisplayRenderer {

    Class<?> rendererClass();

    boolean canRender(String mime);

    DisplayData render(String mime, Object o);
}
