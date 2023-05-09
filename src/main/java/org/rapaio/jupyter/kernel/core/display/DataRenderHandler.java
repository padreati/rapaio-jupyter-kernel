package org.rapaio.jupyter.kernel.core.display;

/**
 * Interface which describes a facility which is able to render
 * objects for notebook output.
 */
public interface DataRenderHandler {

    DataRendererResponse render(Object object);
}
