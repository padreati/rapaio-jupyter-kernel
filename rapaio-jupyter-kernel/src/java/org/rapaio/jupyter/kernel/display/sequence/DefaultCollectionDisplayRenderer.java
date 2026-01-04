package org.rapaio.jupyter.kernel.display.sequence;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.rapaio.jupyter.kernel.display.DisplayData;
import org.rapaio.jupyter.kernel.display.DisplayRenderer;
import org.rapaio.jupyter.kernel.display.MimeType;
import org.rapaio.jupyter.kernel.display.html.Html;
import org.rapaio.jupyter.kernel.global.Global;

public class DefaultCollectionDisplayRenderer implements DisplayRenderer {

    private static final Map<MimeType, Function<Collection<?>, DisplayData>> handlers = Map.of(
            MimeType.TEXT, DefaultCollectionDisplayRenderer::renderText,
            MimeType.HTML, DefaultCollectionDisplayRenderer::renderHtml,
            MimeType.MARKDOWN, DefaultCollectionDisplayRenderer::renderMarkdown
    );

    @Override
    public Class<?> rendererClass() {
        return Collection.class;
    }

    @Override
    public boolean canRender(String mime) {
        return false;
    }

    @Override
    public DisplayData render(String mime, Object o) {
        MimeType mimeType = MimeType.from(mime, Global.options().display().defaultMime());
        Collection<?> collection = (Collection<?>) o;
        return handlers.get(mimeType).apply(collection);
    }

    private static DisplayData renderText(Collection<?> list) {
        int limit = Global.options().display().maxSeqItems();
        return DisplayData.fromText(list.getClass().getSimpleName() + "{"
                + "size:" + list.size() + ",["
                + list.stream().limit(limit).map(Object::toString).map(Html::encode).collect(Collectors.joining(","))
                + (list.size() > limit ? ", ..." : "") + "]}");
    }

    private static DisplayData renderHtml(Collection<?> list) {
        int limit = Global.options().display().maxSeqItems();
        return DisplayData.fromHtml(list.getClass().getSimpleName() + "{"
                + "size:" + list.size() + ",["
                + list.stream().limit(limit).map(Object::toString).collect(Collectors.joining(","))
                + (list.size() > limit ? ", ..." : "") + "]}");
    }

    private static DisplayData renderMarkdown(Collection<?> list) {
        int limit = Global.options().display().maxSeqItems();
        return DisplayData.withType("md",
                list.getClass().getSimpleName() + "{"
                        + "size:" + list.size() + ",["
                        + list.stream().limit(limit).map(Object::toString).collect(Collectors.joining(","))
                        + (list.size() > limit ? ", ..." : "") + "]}");
    }
}
