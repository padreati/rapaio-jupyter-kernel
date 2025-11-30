package org.rapaio.jupyter.kernel.display.html.tags;

import org.rapaio.jupyter.kernel.display.html.HtmlStyle;
import org.rapaio.jupyter.kernel.display.html.Tag;

public class TagB extends Tag {

    public TagB(Tag... tags) {
        super(tags);
    }

    @Override
    public String render(HtmlStyle style) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>");
        children.forEach(c -> sb.append(c.render(style)));
        sb.append("</b>");
        return sb.toString();
    }
}
