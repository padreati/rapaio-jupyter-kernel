package org.rapaio.jupyter.kernel.core.display;

public class DefaultRenderer implements Renderer {

    @Override
    public DisplayData render(Object result, String... args) {
        return DisplayData.withText(result.toString());
    }
}
