package org.rapaio.jupyter.kernel.core.display;

public interface Renderer {

    DisplayData render(Object result);
}
