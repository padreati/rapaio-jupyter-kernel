package org.rapaio.jupyter.kernel.core.display.html.tags;

import org.rapaio.jupyter.kernel.core.display.html.HtmlStyle;
import org.rapaio.jupyter.kernel.core.display.html.Tag;
import org.rapaio.jupyter.kernel.core.display.html.Tags;

import java.util.function.Supplier;

public class TagIf extends Tag {

    public TagIf(boolean condition, Supplier<Tag[]> supplier) {
        super(condition ? supplier.get() : new Tag[]{Tags.empty()});
    }

    @Override
    public String render(HtmlStyle style) {
        StringBuilder sb = new StringBuilder();
        for (var child : children) {
            sb.append(child.render(style));
        }
        return sb.toString();
    }
}
