package org.rapaio.jupyter.kernel.display.html.tags;

import java.util.function.Supplier;

import org.rapaio.jupyter.kernel.display.html.HtmlStyle;
import org.rapaio.jupyter.kernel.display.html.Tag;
import org.rapaio.jupyter.kernel.display.html.Tags;

public class TagIf extends Tag {

    public TagIf(boolean condition, Supplier<Tag[]> positive) {
        this(condition, positive, null);
    }

    public TagIf(boolean condition, Supplier<Tag[]> positive, Supplier<Tag[]> negative) {
        super(condition ? positive.get() : (negative != null ? negative.get() : new Tag[] {Tags.empty()}));
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
