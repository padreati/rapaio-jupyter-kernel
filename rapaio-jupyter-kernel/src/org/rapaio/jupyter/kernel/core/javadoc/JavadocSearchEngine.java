package org.rapaio.jupyter.kernel.core.javadoc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple search engine for javadoc elements.
 * <p>
 * The search is done using the search signatures which are built during
 * javadoc parsing step.
 */
public class JavadocSearchEngine {

    private final Map<String, JavadocElement> searchMap = new HashMap<>();

    public void reset() {
        searchMap.clear();
    }

    public void add(String absolutePath) {
        List<JavadocElement> elements = new JavadocJarParser().parse(absolutePath);
        if (elements != null) {
            for (var element : elements) {
                for (String searchSignature : element.searchSignatures()) {
                    searchMap.put(searchSignature, element);
                }
            }
        }
    }

    public String search(String signature) {
        if (searchMap.containsKey(signature)) {
            return searchMap.get(signature).description();
        }
        return "";
    }
}
