package org.rapaio.jupyter.display.provider.list;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.rapaio.jupyter.kernel.display.DisplayData;
import org.rapaio.jupyter.kernel.display.DisplayRenderer;
import org.rapaio.jupyter.kernel.display.MimeType;
import org.rapaio.jupyter.kernel.global.Global;

public class SetDisplayRenderer implements DisplayRenderer {

    private static final Map<MimeType, Function<Set<?>, DisplayData>> handlers = Map.of(
            MimeType.TEXT, new TextListHandler(),
            MimeType.HTML, new HtmlListHandler()
    );

    @Override
    public Class<?> rendererClass() {
        return Set.class;
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
        return handler.apply((Set<?>) o);
    }

    static class TextListHandler implements Function<Set<?>, DisplayData> {

        @Override
        public DisplayData apply(Set<?> collection) {
            String sb = collection.getClass().getSimpleName() + "{"
                    + "size:" + collection.size() + ",["
                    + collection.stream().limit(10).map(Object::toString).collect(Collectors.joining(","))
                    + (collection.size() > 10 ? ", ..." : "") + "]}";
            return DisplayData.fromText(sb);
        }
    }

    static class HtmlListHandler implements Function<Set<?>, DisplayData> {

        @Override
        public DisplayData apply(Set<?> list) {
            String sb = "<b>" + list.getClass().getSimpleName() + "</b>{"
                    + "size:<b>" + list.size() + "</b>,["
                    + list.stream().limit(10).map(Object::toString).collect(Collectors.joining(","))
                    + (list.size() > 10 ? ", ..." : "") + "]}";

            return DisplayData.fromHtml(sb);
        }
    }
}
