package org.rapaio.jupyter.display.provider.list;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.rapaio.jupyter.kernel.display.DisplayData;
import org.rapaio.jupyter.kernel.display.DisplayRenderer;
import org.rapaio.jupyter.kernel.display.MimeType;
import org.rapaio.jupyter.kernel.global.Global;

public class ListDisplayRenderer implements DisplayRenderer {

    private static final Map<MimeType, Function<List<?>, DisplayData>> handlers = Map.of(
            MimeType.TEXT, new TextListHandler(),
            MimeType.HTML, new HtmlListHandler()
    );

    @Override
    public Class<?> rendererClass() {
        return List.class;
    }

    @Override
    public boolean canRender(String mime) {

        MimeType mimeType = MimeType.from(mime, Global.config().display().defaultMime());
        if (handlers.containsKey(mimeType)) {
            return true;
        }
        return false;
    }

    @Override
    public DisplayData render(String mime, Object o) {
        if (!canRender(mime)) {
            return null;
        }
        MimeType mimeType = MimeType.from(mime, Global.config().display().defaultMime());
        var handler = handlers.get(mimeType);
        return handler.apply((List<?>) o);
    }

    static class TextListHandler implements Function<List<?>, DisplayData> {

        @Override
        public DisplayData apply(List<?> collection) {
            String sb = collection.getClass().getSimpleName() + "{"
                    + "size:" + collection.size() + ",["
                    + collection.stream().limit(10).map(Object::toString).collect(Collectors.joining(","))
                    + (collection.size() > 10 ? ", ..." : "") + "]}";
            return DisplayData.fromText(sb);
        }
    }

    static class HtmlListHandler implements Function<List<?>, DisplayData> {

        @Override
        public DisplayData apply(List<?> list) {
            String sb = "<b>" + list.getClass().getSimpleName() + "</b>{"
                    + "size:<b>" + list.size() + "</b>,["
                    + list.stream().limit(10).map(Object::toString).collect(Collectors.joining(","))
                    + (list.size() > 10 ? ", ..." : "") + "]}";

            return DisplayData.fromHtml(sb);
        }
    }
}
