package org.rapaio.jupyter.kernel.core.javadoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 * Contains the parsing logic which extracts documentation snippets from javadoc dependency jar files.
 * <p>
 * It implements the following steps:
 * - search for index-all.html file
 * - parse all links from index file
 * - for each link parse the corresponding documentation page
 * - extract documentation snippets and signatures
 */
public class JavadocJarParser {

    public static String javadocPreprocess(String javadoc) {
        javadoc = javadoc.replaceAll("\n", "<br/>");
        return javadoc;
    }

    /**
     * Consider if a jar file contains javadoc data or not. A javadoc jar file
     * is one which contains index-all.html file inside.
     *
     * @param absolutePath absolute path of the jar archive
     * @return true if the jar file contains javadoc data
     */
    public static boolean checkIfJavadoc(String absolutePath) {
        try (JarFile jar = new JarFile(new File(absolutePath), true)) {
            Iterator<JarEntry> jarEntryIterator = jar.entries().asIterator();
            while (jarEntryIterator.hasNext()) {
                JarEntry entry = jarEntryIterator.next();
                if (!entry.isDirectory() && "index-all.html".equals(entry.getRealName())) {
                    return true;
                }
            }
            return false;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Parses a javadoc jar file and extracts a list of identified documentation snippets.
     *
     * @param jarPath jar path
     * @return list of javadoc elements
     */
    public List<JavadocElement> parse(String jarPath) {
        List<JavadocElement> elements = new ArrayList<>();
        try (JarFile jar = new JarFile(jarPath)) {

            // find index-all.html
            JarEntry indexEntry = jar.stream()
                    .filter(e -> "index-all.html".equals(e.getRealName()))
                    .findFirst()
                    .orElse(null);
            if (indexEntry == null) {
                return null;
            }

            // parse link values from inde-all.html
            SortedMap<String, IndexLink> indexes = parseIndex(jar, indexEntry);

            ExecutorService es = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));

            // for each link parse the documentation page in parallel threads
            List<Future<JavadocElement>> futures = new ArrayList<>();
            for (var e : indexes.entrySet()) {
                futures.add(es.submit(() -> {
                    String href = e.getKey();
                    String path = href.contains("#") ? href.substring(0, href.indexOf('#')) : href;
                    JarEntry entry = jar.getJarEntry(path);
                    if (entry == null) {
                        return null;
                    }
                    return parseDocPage(jar, href, e.getValue().linkData, entry);
                }));
            }
            while (!futures.isEmpty()) {
                Iterator<Future<JavadocElement>> it = futures.iterator();
                while (it.hasNext()) {
                    Future<JavadocElement> future = it.next();
                    if (future.isDone()) {
                        it.remove();
                        JavadocElement elem = null;
                        try {
                            elem = future.get();
                        } catch (Exception e) {
                            // skip
                        }
                        if (elem != null) {
                            elements.add(elem);
                        }
                    }
                }
            }
            return elements;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Parses the index-all.html and extracts href references to documentation pages.
     *
     * @param jar jar file object
     * @param entry entry of interest
     * @return map with href as key and link data as value
     * @throws IOException
     */
    private SortedMap<String, IndexLink> parseIndex(JarFile jar, JarEntry entry) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(jar.getInputStream(entry)))) {
            IndexParser parser = new IndexParser();
            new ParserDelegator().parse(reader, parser, true);
            return parser.refs;
        }
    }

    private record IndexLink(String href, String linkType, String linkData) {
    }

    private static class IndexParser extends HTMLEditorKit.ParserCallback {

        final TreeMap<String, IndexLink> refs = new TreeMap<>();
        boolean inDL, inDT, inA;
        String href;
        String linkType;

        @Override
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            if (t == HTML.Tag.DL && a.containsAttribute(HTML.Attribute.CLASS, "index")) {
                inDL = true;
            }
            if (inDL && t == HTML.Tag.DT) {
                inDT = true;
            }
            if (inDT && t == HTML.Tag.A) {
                if (a.getAttribute(HTML.Attribute.CLASS) == null) {
                    return;
                }
                inA = true;
                Object h = a.getAttribute(HTML.Attribute.HREF);
                if (h != null) {
                    href = h.toString();
                }
                Object lt = a.getAttribute(HTML.Attribute.CLASS);
                if (lt != null) {
                    linkType = lt.toString();
                }
            }
        }

        @Override
        public void handleText(char[] data, int pos) {
            if (inA && href != null) {
                if (!"external-link".equals(linkType)) {
                    refs.put(href, new IndexLink(href, linkType, new String(data)));
                }
                href = null;
                linkType = null;
            }
        }

        @Override
        public void handleEndTag(HTML.Tag t, int pos) {
            if (t == HTML.Tag.DL) {
                inDL = false;
            }
            if (t == HTML.Tag.DT) {
                inDT = false;
            }
            if (t == HTML.Tag.A) {
                inA = false;
            }
        }
    }

    public JavadocElement parseDocPage(JarFile jar, String href, String linkData, JarEntry entry) {
        try (InputStream in = jar.getInputStream(entry)) {
            JavadocType type = JavadocType.determineType(href, linkData);
            return switch (type) {
                case FIELD, CONSTRUCTOR, METHOD -> parseMemberDocPage(in, href, linkData, type);
                case CLASS -> parseClassDocPage(in, href, linkData);
                default -> null;
            };
        } catch (IOException e) {
            // skip
        }
        return null;
    }

    private String parseTypeName(String href) {
        String type = href;
        if (type.contains(".html")) {
            type = type.substring(0, type.lastIndexOf(".html"));
        }
        return type.substring(type.lastIndexOf("/") + 1);
    }

    private String parsePackageName(Document doc, String href) {
        var it = doc.body().stream().iterator();
        while (it.hasNext()) {
            Element el = it.next();
            if (el.nameIs("a") && "package-summary.html".equals(el.attr("href"))) {
                return el.text();
            }
        }
        return null;
    }

    public JavadocElement parseClassDocPage(InputStream in, String href, String linkData) {
        try {
            Document doc = Jsoup.parse(in, StandardCharsets.UTF_8.name(), href);
            String packageName = parsePackageName(doc, href);
            String typeName = parseTypeName(href);
            var it = doc.body().stream().iterator();
            while (it.hasNext()) {
                Element el = it.next();
                if (el.nameIs("section") && "class-description".equals(el.attr("id")) && "class-description".equals(el.attr("class"))) {

                    Element hs = el.children().stream()
                            .filter(e -> e.nameIs("div"))
                            .filter(e -> "horizontal-scroll".equals(e.attr("class")))
                            .findFirst()
                            .orElse(null);

                    Element parent = (hs != null) ? hs : el;

                    Element ms = parent.children().stream()
                            .filter(e -> e.nameIs("div"))
                            .filter(e -> "type-signature".equals(e.attr("class")))
                            .findFirst()
                            .orElse(null);
                    String memberSignature = getSignature(ms);

                    var divBlocks = parent.children().stream()
                            .filter(e -> e.nameIs("div"))
                            .filter(e -> "block".equals(e.attr("class")))
                            .toList();

                    StringBuilder descriptionBuilder = new StringBuilder();
                    for (var divBlock : divBlocks) {
                        String html = divBlock.html();
                        if (html.contains("Description copied from")) {
                            continue;
                        }
                        descriptionBuilder.append(html).append("</br>");
                    }
                    String description = descriptionBuilder.toString();

                    List<String> searchSignature =
                            createSearchSignatures(href, linkData, JavadocType.CLASS, memberSignature, packageName, typeName);
                    return new JavadocElement(href, linkData, JavadocType.CLASS, packageName, typeName, memberSignature, searchSignature,
                            description);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public JavadocElement parseMemberDocPage(InputStream in, String href, String linkData, JavadocType type) {
        try {
            String id = URLDecoder.decode(href.substring(href.indexOf('#') + 1), StandardCharsets.UTF_8);
            Document doc = Jsoup.parse(in, StandardCharsets.UTF_8.name(), href);
            String packageName = parsePackageName(doc, href);
            String typeName = parseTypeName(href);
            var it = doc.body().stream().iterator();
            while (it.hasNext()) {
                Element el = it.next();
                if (el.nameIs("section") && id.equals(el.attr("id")) && "detail".equals(el.attr("class"))) {

                    Element hs = el.children().stream()
                            .filter(e -> e.nameIs("div"))
                            .filter(e -> "horizontal-scroll".equals(e.attr("class")))
                            .findFirst()
                            .orElse(null);

                    Element parent = (hs != null) ? hs : el;

                    Element ms = parent.children().stream()
                            .filter(e -> e.nameIs("div"))
                            .filter(e -> "member-signature".equals(e.attr("class")))
                            .findFirst()
                            .orElse(null);
                    String memberSignature = getSignature(ms);

                    var divBlocks = parent.children().stream()
                            .filter(e -> e.nameIs("div"))
                            .filter(e -> "block".equals(e.attr("class")))
                            .toList();

                    StringBuilder descriptionBuilder = new StringBuilder();
                    for (var divBlock : divBlocks) {
                        String html = divBlock.html();
                        if (html.contains("Description copied from")) {
                            continue;
                        }
                        if (!descriptionBuilder.isEmpty()) {
                            descriptionBuilder.append("</br>");
                        }
                        descriptionBuilder.append(html);
                    }

                    var notes = parent.children().stream()
                            .filter(e -> e.nameIs("dl"))
                            .filter(e -> "notes".equals(e.attr("class")))
                            .toList();
                    for (var note : notes) {
                        if (!descriptionBuilder.isEmpty()) {
                            descriptionBuilder.append("</br>");
                        }
                        descriptionBuilder.append(note.html());
                    }

                    String description = descriptionBuilder.toString();

                    List<String> searchSignatures = createSearchSignatures(href, linkData, type, memberSignature, packageName, typeName);
                    return new JavadocElement(href, linkData, type, packageName, typeName, memberSignature, searchSignatures, description);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String getSignature(Element ms) {
        if (ms == null) {
            return null;
        }

        String rawString = ms.childNodes().stream().map(ne -> {
            if (ne instanceof TextNode) {
                return ne.nodeValue();
            }
            return ne.childNodes().stream().map(Node::nodeValue).collect(Collectors.joining());
        }).collect(Collectors.joining());

        rawString = URLDecoder.decode(rawString, StandardCharsets.UTF_8);
        rawString = rawString.replaceAll("\n", "");
        int[] buffer = rawString.codePoints().map(cp -> cp == 160 ? 32 : cp).toArray();
        rawString = new String(buffer, 0, buffer.length);
        rawString = rawString.replaceAll("[ ]+", " ");
        return rawString;
    }

    private static final String[] modifiers = new String[] {
            "public ", "private ", "protected ", "final ", "static ", "volatile "
    };

    public List<String> createSearchSignatures(String href, String linkData, JavadocType type, String memberSignature, String packageName,
            String typeName) {

        String className = href.substring(href.lastIndexOf("/") + 1, href.indexOf(".html"));

        if (type == JavadocType.FIELD || type == JavadocType.CONSTRUCTOR || type == JavadocType.METHOD) {
            String searchSignature = memberSignature;


            // remove modifiers
            for (String modifier : modifiers) {
                if (searchSignature.contains(modifier)) {
                    searchSignature = searchSignature.replaceAll(modifier, "").strip();
                }
            }

            // remove return type
            String name = linkData.split("\\b")[0];
            int index = searchSignature.indexOf(name);

            if (type == JavadocType.FIELD) {
                return List.of(searchSignature.substring(0, index) + className + "." + searchSignature.substring(index));
            }

            if (type == JavadocType.CONSTRUCTOR) {
                return List.of(searchSignature.substring(0, index) + className + searchSignature.substring(index));
            }

            String prefix = searchSignature.substring(0, index);
            String postfix = searchSignature.substring(index);
            String parameterList = postfix.substring(postfix.indexOf('(') + 1);
            parameterList = parameterList.substring(0, parameterList.length() - 1);

            String[] parameters = parameterList.isEmpty() ? new String[0] : parameterList.split(",");
            String trimmedParameterList = Arrays.stream(parameters)
                    .map(s -> {
                        int i = s.lastIndexOf(' ');
                        return (i == -1) ? s : s.substring(0, i);
                    })
                    .collect(Collectors.joining(","));
            return List.of(
                    prefix + className + "." + postfix,
                    prefix + className + "." + postfix.substring(0, postfix.indexOf('(') + 1) + trimmedParameterList + ')');
        }

        if (type == JavadocType.CLASS || type == JavadocType.ENUM) {
            return List.of(packageName + "." + typeName);
        }

        return List.of();
    }

}
