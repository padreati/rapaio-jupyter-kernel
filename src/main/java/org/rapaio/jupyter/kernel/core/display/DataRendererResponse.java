package org.rapaio.jupyter.kernel.core.display;

public record DataRendererResponse(boolean handled, DisplayData displayData) {

    public static DataRendererResponse unhandled() {
        return new DataRendererResponse(false, null);
    }
}
