package org.rapaio.jupyter.kernel.core.display.html;

import java.util.Collection;
import java.util.function.Function;

import org.rapaio.jupyter.kernel.core.display.html.tags.TagB;
import org.rapaio.jupyter.kernel.core.display.html.tags.TagBr;
import org.rapaio.jupyter.kernel.core.display.html.tags.TagEach;
import org.rapaio.jupyter.kernel.core.display.html.tags.TagIf;
import org.rapaio.jupyter.kernel.core.display.html.tags.TagJoin;
import org.rapaio.jupyter.kernel.core.display.html.tags.TagP;
import org.rapaio.jupyter.kernel.core.display.html.tags.TagTexts;

public class Tags {

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

    public static TagIf iif(boolean cond, Tag... tags) {
        return new TagIf(cond, tags);
    }
}
