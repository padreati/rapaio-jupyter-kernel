package org.rapaio.jupyter.kernel.core.display;

public class DefaultRenderer implements Renderer {

    @Override
    public DisplayData render(Object result, String...args) {
        DisplayData dd = new DisplayData();
        dd.putText(result.toString());
        return dd;
    }
}
