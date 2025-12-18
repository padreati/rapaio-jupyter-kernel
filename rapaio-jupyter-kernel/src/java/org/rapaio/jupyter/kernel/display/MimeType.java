package org.rapaio.jupyter.kernel.display;

import java.util.Set;

public enum MimeType {

    TEXT(Set.of("text"), "text/plain"),
    HTML(Set.of("html"), "text/html"),
    MARKDOWN(Set.of("markdown", "md"), "text/markdown"),
    JAVASCRIPT(Set.of("javascript"), "application/javascript"),
    JSON(Set.of("json"), "application/json"),
    LATEX(Set.of("latex"), "text/latex"),

    JPEG(Set.of("jpeg", "jpg"), "image/jpeg"),
    GIF(Set.of("gif"), "image/gif"),
    PNG(Set.of("png"), "image/png");

    private final Set<String> shortTypes;
    private final String longType;

    MimeType(Set<String> shortTypes, String stringType) {
        this.shortTypes = shortTypes;
        this.longType = stringType;
    }

    public Set<String> shortTypes() {
        return shortTypes;
    }

    public String firstShortType() {
        return shortTypes.iterator().next();
    }

    public String longType() {
        return longType;
    }

    public static MimeType from(String code, String defaultValue) {
        String key = (code == null) ? defaultValue : code;
        for (MimeType mimeType : MimeType.values()) {
            if (mimeType.shortTypes.contains(key) || mimeType.longType.equals(key) || mimeType.name().equals(key)) {
                return mimeType;
            }
        }
        throw new IllegalArgumentException("Unknown mime type: " + code);
    }

    @Override
    public String toString() {
        return longType;
    }
}
