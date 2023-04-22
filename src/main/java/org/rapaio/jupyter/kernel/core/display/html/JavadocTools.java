package org.rapaio.jupyter.kernel.core.display.html;

/**
 * A place to write custom stuff for javadoc pretty rendering in html.
 */
public final class JavadocTools {

    private JavadocTools() {
    }

    public static String javadocPreprocess(String javadoc) {
        javadoc = javadoc.replaceAll("\n", "<br/>");
        return javadoc;
    }
}
