package org.rapaio.jupyter.kernel.core.javadoc;

public enum JavadocType {
    MODULE,
    PACKAGE,
    CLASS,
    INTERFACE,
    ENUM,
    ANNOTATION,
    METHOD,
    CONSTRUCTOR,
    FIELD;

    public static JavadocType determineType(String href, String name) {
        if (href.contains("/package-summary.html")) {
            return JavadocType.PACKAGE;
        }
        if (href.contains("/module-summary.html")) {
            return JavadocType.MODULE;
        }
        if (href.contains("#")) {
            if(name.contains("<init>")) {
                return JavadocType.CONSTRUCTOR;
            }
            if(name.contains("(")) {
                return JavadocType.METHOD;
            }
            return JavadocType.FIELD;
        }
        return JavadocType.CLASS;
    }
}
