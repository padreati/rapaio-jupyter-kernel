package org.rapaio.jupyter.kernel.core.javadoc;

import java.util.List;

/**
 * Record which describes a javadoc element with its syntactical parts.
 *
 * @param href             hyperlink reference inside javadoc jar file
 * @param linkData         local reference (empty for types)
 * @param javadocType      the syntactical Java type
 * @param packageName      package name
 * @param typeName         class/interface/enum name
 * @param signature        complete signature of the element
 * @param searchSignatures signature in the form searched by jshell
 * @param description      html documentation for the given element
 */
public record JavadocElement(String href, String linkData, JavadocType javadocType, String packageName, String typeName, String signature,
                             List<String> searchSignatures, String description) {
}

