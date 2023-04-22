package org.rapaio.jupyter.kernel.core.display.html.tags;

import org.rapaio.jupyter.kernel.core.display.html.HtmlStyle;
import org.rapaio.jupyter.kernel.core.display.html.Tag;

public class TagJoin extends Tag {

    public TagJoin(Tag... tags) {
        super(tags);
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
