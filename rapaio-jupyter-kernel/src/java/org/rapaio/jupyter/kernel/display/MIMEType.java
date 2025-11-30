package org.rapaio.jupyter.kernel.display;

public final class MIMEType {

    public static final MIMEType TEXT = new MIMEType("text", "text/plain");
    public static final MIMEType HTML = new MIMEType("html", "text/html");
    public static final MIMEType MARKDOWN = new MIMEType("markdown", "text/markdown");
    public static final MIMEType JAVASCRIPT = new MIMEType("javascript", "application/javascript");
    public static final MIMEType JSON = new MIMEType("json", "application/json");
    public static final MIMEType LATEX = new MIMEType("latex", "text/latex");

    public static final MIMEType JPEG = new MIMEType("jpeg", "image/jpeg");
    public static final MIMEType GIF = new MIMEType("gif", "image/gif");
    public static final MIMEType PNG = new MIMEType("png", "image/png");

    public static MIMEType fromString(String code) {
        if (code == null) {
            return null;
        }
        if (TEXT.shortType.equals(code) || TEXT.stringType.equals(code)) {
            return TEXT;
        }
        if (HTML.shortType.equals(code) || HTML.stringType.equals(code)) {
            return HTML;
        }
        if (MARKDOWN.shortType.equals(code) || MARKDOWN.stringType.equals(code)) {
            return MARKDOWN;
        }
        if (JAVASCRIPT.shortType.equals(code) || JAVASCRIPT.stringType.equals(code)) {
            return JAVASCRIPT;
        }
        if (JSON.shortType.equals(code) || JSON.stringType.equals(code)) {
            return JSON;
        }
        if (LATEX.shortType.equals(code) || LATEX.stringType.equals(code)) {
            return LATEX;
        }
        if (JPEG.shortType.equals(code) || JPEG.stringType.equals(code)) {
            return JPEG;
        }
        if (GIF.shortType.equals(code) || GIF.stringType.equals(code)) {
            return GIF;
        }
        if (PNG.shortType.equals(code) || PNG.stringType.equals(code)) {
            return PNG;
        }
        return null;
    }

    private final String shortType;
    private final String stringType;

    private MIMEType(String shortType, String stringType) {
        this.shortType = shortType;
        this.stringType = stringType;
    }

    @Override
    public String toString() {
        return stringType;
    }
}
