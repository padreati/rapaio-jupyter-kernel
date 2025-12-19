package org.rapaio.jupyter.kernel.display;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;

import org.rapaio.jupyter.kernel.display.image.DefaultImageDisplayRenderer;
import org.rapaio.jupyter.kernel.display.spi.DisplayRendererProvider;
import org.rapaio.jupyter.kernel.display.spi.DisplayTransformerProvider;
import org.rapaio.jupyter.kernel.display.table.DefaultTableDisplayRenderer;

/**
 * This class is responsible with the display of objects in the notebook cell's output.
 */
public final class DisplaySystem {

    private static final DisplaySystem inst = new DisplaySystem();

    public static DisplaySystem inst() {
        return inst;
    }

    private final List<DisplayTransformer> spiDisplayTransformers = new ArrayList<>();
    private final List<DisplayRenderer> spiDisplayRenderers = new ArrayList<>();

    private final List<DisplayTransformer> systemDisplayTransformers = new ArrayList<>();
    private final List<DisplayRenderer> systemDisplayRenderers = new ArrayList<>();

    private final DefaultDisplayRenderer defaultDisplayRenderer = new DefaultDisplayRenderer();

    private final ServiceLoader<DisplayTransformerProvider> transformerLoader;
    private final ServiceLoader<DisplayRendererProvider> rendererLoader;

    private DisplaySystem() {
        systemDisplayRenderers.add(new DefaultImageDisplayRenderer());
        systemDisplayRenderers.add(new DefaultTableDisplayRenderer());

        transformerLoader = ServiceLoader.load(DisplayTransformerProvider.class);
        rendererLoader = ServiceLoader.load(DisplayRendererProvider.class);

        refreshSpiDisplayHandlers();
    }

    public void refreshSpiDisplayHandlers() {
        transformerLoader.reload();
        rendererLoader.reload();

        spiDisplayTransformers.clear();
        spiDisplayRenderers.clear();

        transformerLoader.stream()
                .flatMap(provider -> provider.get().getDisplayTransformers().stream())
                .forEach(spiDisplayTransformers::add);
        rendererLoader.stream().flatMap(provider -> provider.get().getDisplayRenderers().stream()).forEach(spiDisplayRenderers::add);
    }

    /**
     * Produces the display data which will be passed to the notebook cell's output
     * from an object. The mime type is determined by the global configuration.
     *
     * @param o object to be rendered
     * @return produced display data
     */
    public DisplayData render(Object o) {
        return render(null, o);
    }

    /**
     * Produces the display data which will be passed to the notebook cell's output
     * from an object and an optional mime type.
     *
     * @param mime desired mime type of the display data
     * @param o    object to be rendered
     * @return produced display data
     */
    public DisplayData render(String mime, Object o) {

        DisplayData dd = tryRender(mime, o);
        if (dd != null) {
            return dd;
        }

        for (var transformer : spiDisplayTransformers) {
            if (transformer.canTransform(o)) {
                Object transformed = transformer.transform(o);
                dd = tryRender(mime, transformed);
                if (dd != null) {
                    return dd;
                }
            }
        }
        for (var transformer : systemDisplayTransformers) {
            if (transformer.canTransform(o)) {
                Object transformed = transformer.transform(o);
                dd = tryRender(mime, transformed);
                if (dd != null) {
                    return dd;
                }
            }
        }
        return withId(defaultDisplayRenderer.render(mime, o));
    }

    private DisplayData tryRender(String mime, Object o) {
        for (var renderer : spiDisplayRenderers) {
            if (renderer.rendererClass().isAssignableFrom(o.getClass())) {
                if (renderer.canRender(mime)) {
                    return withId(renderer.render(mime, o));
                }
            }
        }
        for (var renderer : systemDisplayRenderers) {
            if (renderer.rendererClass().isAssignableFrom(o.getClass())) {
                return withId(renderer.render(mime, o));
            }
        }
        return null;
    }

    private DisplayData withId(DisplayData displayData) {
        if (!displayData.hasDisplayId()) {
            displayData.setDisplayId(UUID.randomUUID().toString());
        }
        return displayData;
    }
}
