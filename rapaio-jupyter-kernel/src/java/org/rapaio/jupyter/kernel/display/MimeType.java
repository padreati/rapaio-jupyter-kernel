package org.rapaio.jupyter.kernel.display;

public final class MimeType {

    public static final MimeType TEXT = new MimeType("text", "text/plain");
    public static final MimeType HTML = new MimeType("html", "text/html");
    public static final MimeType MARKDOWN = new MimeType("markdown", "text/markdown");
    public static final MimeType JAVASCRIPT = new MimeType("javascript", "application/javascript");
    public static final MimeType JSON = new MimeType("json", "application/json");
    public static final MimeType LATEX = new MimeType("latex", "text/latex");

    public static final MimeType JPEG = new MimeType("jpeg", "image/jpeg");
    public static final MimeType GIF = new MimeType("gif", "image/gif");
    public static final MimeType PNG = new MimeType("png", "image/png");

    public static MimeType fromString(String code) {
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

    private MimeType(String shortType, String stringType) {
        this.shortType = shortType;
        this.stringType = stringType;
    }

    @Override
    public String toString() {
        return stringType;
    }
}
