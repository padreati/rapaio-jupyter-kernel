package org.rapaio.jupyter.kernel.display;

public class DefaultDisplayHandler implements DisplayHandler {

    @Override
    public boolean canRender(Object o) {
        return true;
    }

    @Override
    public String defaultMIMEType() {
        return MIMEType.TEXT.toString();
    }

    @Override
    public DisplayData render(String mimeType, Object o) {
        if (o == null) {
            return null;
        }
        return DisplayData.withType(mimeType, o.toString());
    }

}
