package org.rapaio.jupyter.kernel.core.display;

public final class MIMEType {

    public static final MIMEType TEXT = new MIMEType("text/plain");
    public static final MIMEType HTML = new MIMEType("text/html");
    public static final MIMEType MARKDOWN = new MIMEType("text/markdown");
    public static final MIMEType JAVASCRIPT = new MIMEType("application/javascript");
    public static final MIMEType JSON = new MIMEType("application/json");
    public static final MIMEType LATEX = new MIMEType("text/latex");

    public static final MIMEType JPEG = new MIMEType("image/jpeg");
    public static final MIMEType GIF = new MIMEType("image/gif");
    public static final MIMEType PNG = new MIMEType("image/png");
    public static final MIMEType SVG = new MIMEType("image/svg+xml");

    public static MIMEType fromString(String type) {
        return new MIMEType(type);
    }

    private final String stringType;

    private MIMEType(String stringType) {
        this.stringType = stringType;
    }

    @Override
    public String toString() {
        return stringType;
    }
}
