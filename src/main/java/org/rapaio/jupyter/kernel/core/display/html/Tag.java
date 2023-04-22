package org.rapaio.jupyter.kernel.core.display.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Tag {

    private static final HtmlStyle defaultStyle = new BaseHtmlStyle();
    protected final List<Tag> children;

    public Tag(Tag...children) {
        this.children = new ArrayList<>(Arrays.asList(children));
    }

    public abstract String render(HtmlStyle style);

    public String render() {
        return render(defaultStyle);
    }
}
