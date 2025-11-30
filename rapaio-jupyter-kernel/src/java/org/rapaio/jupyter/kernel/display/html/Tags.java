package org.rapaio.jupyter.kernel.display.html;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import org.rapaio.jupyter.kernel.display.html.tags.TagB;
import org.rapaio.jupyter.kernel.display.html.tags.TagBr;
import org.rapaio.jupyter.kernel.display.html.tags.TagEach;
import org.rapaio.jupyter.kernel.display.html.tags.TagIf;
import org.rapaio.jupyter.kernel.display.html.tags.TagJoin;
import org.rapaio.jupyter.kernel.display.html.tags.TagP;
import org.rapaio.jupyter.kernel.display.html.tags.TagTexts;

public class Tags {

    public static Tag empty() {
        return new Tag() {
            @Override
            public String render(HtmlStyle style) {
                return "";
            }
        };
    }

    public static TagJoin join(Tag... nodes) {
        return new TagJoin(nodes);
    }

    public static <T> TagEach each(Collection<T> tags, Function<T, Tag> mapper) {
        return new TagEach(tags.stream().map(mapper).toArray(Tag[]::new));
    }

    public static TagP p(Tag... nodes) {
        return new TagP(nodes);
    }

    public static TagBr br() {
        return new TagBr();
    }

    public static TagB b(Tag... tags) {
        return new TagB(tags);
    }

    public static TagTexts texts(String... texts) {
        return new TagTexts(texts);
    }

    public static TagTexts space(int repeat) {
        return new TagTexts("&nbsp;".repeat(repeat));
    }

    public static TagIf iif(boolean cond, Supplier<Tag[]> supplier) {
        return new TagIf(cond, supplier);
    }
}
