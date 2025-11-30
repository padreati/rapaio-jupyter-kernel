package org.rapaio.jupyter.kernel.display.html.tags;

import org.rapaio.jupyter.kernel.display.html.HtmlStyle;
import org.rapaio.jupyter.kernel.display.html.Tag;

public class TagBr extends Tag {

    public TagBr() {
        super();
    }

    @Override
    public String render(HtmlStyle style) {
        return "<br/>";
    }
}
