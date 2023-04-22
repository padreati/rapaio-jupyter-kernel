package org.rapaio.jupyter.kernel.core.display.html.tags;

import org.rapaio.jupyter.kernel.core.display.html.HtmlStyle;
import org.rapaio.jupyter.kernel.core.display.html.Tag;

public class TagP extends Tag {

    public TagP(Tag... children) {
        super(children);
    }

    @Override
    public String render(HtmlStyle style) {
        StringBuilder sb = new StringBuilder();
        if(style.styleFor(this).isEmpty()) {
            sb.append("<p>");
        } else {
            sb.append("<p style=\"").append(style.styleFor(this)).append("\">");
        }
        children.forEach(c -> sb.append(c.render(style)));
        sb.append("</p>");
        return sb.toString();
    }
}
