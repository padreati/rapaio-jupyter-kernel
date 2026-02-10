package org.rapaio.jupyter.kernel.javadoc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.core.javadoc.JavadocElement;
import org.rapaio.jupyter.kernel.core.javadoc.JavadocJarParser;
import org.rapaio.jupyter.kernel.core.javadoc.JavadocType;

public class JavadocJarParserTest {

    private final JavadocJarParser parser = new JavadocJarParser();
    private Map<String, String> memberTexts;

    @BeforeEach
    void beforeEach() {
        memberTexts = new HashMap<>();
        memberTexts.put("""
                        <div class="member-signature"><span class="modifiers">public final</span>&nbsp;<span class="return-type">boolean</span>
                        &nbsp;<span class="element-name">equals</span><wbr><span class="parameters">(
                        <a href="https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Object.html" 
                        title="class or interface in java.lang" class="external-link">Object</a>&nbsp;o)</span></div>""",
                "public final boolean equals(Object o)");
        memberTexts.put("""
                        <div class="member-signature"><span class="modifiers">public</span>&nbsp;<span class="return-type"><a href="../data/Frame.html" title="interface in rapaio.data">Frame</a></span>&nbsp;<span class="element-name">trainDf</span>()</div>""",
                "public Frame trainDf()");
        memberTexts.put("""
                        <div class="member-signature"><span class="modifiers">private static</span>&nbsp;<span class="return-type">void</span>&nbsp;<span class="element-name">normalize</span><wbr><span class="parameters">(double[]&nbsp;freq)</span></div>""",
                "private static void normalize(double[] freq)");
    }

    @Test
    void debugSample() {

        String href = "file:///./rapaio.lib/rapaio/graphics/plot/artist/ABLine.html";
        String id = "ABLine";
        JavadocElement element = parser.parseClassDocPage(getClass().getResourceAsStream("sample.html"), href, id);
        assertEquals(href, element.href());
        assertEquals("ABLine", element.linkData());
        assertEquals(JavadocType.CLASS, element.javadocType());
        assertEquals("public class ABLine extends Artist", element.signature());
        assertEquals(1, element.searchSignatures().size());
        assertEquals("rapaio.graphics.plot.artist.ABLine", element.searchSignatures().get(0));
        assertEquals("""
                Artist which draws a line of the form y = f(x) = a*x + b There is a generic form of the line by calling <a href="#%3Cinit%3E(double,double,rapaio.graphics.opt.GOpt...)"><code>ABLine(double, double, GOpt...)</code></a>.
                <p>Also there is a simpler form for drawing horizontal or vertical lines. The simpler form is called by using <a href="#%3Cinit%3E(boolean,double,rapaio.graphics.opt.GOpt...)"><code>ABLine(boolean, double, GOpt...)</code></a>, with the boolean parameter specifying if the line is horizontal or vertical.</p>
                <p>Also there are two dedicated shortcuts in plotter <a href="../../Plotter.html#hLine(double,rapaio.graphics.opt.GOpt...)"><code>Plotter.hLine(double, GOpt...)</code></a> and <a href="../../Plotter.html#vLine(double,rapaio.graphics.opt.GOpt...)"><code>Plotter.vLine(double, GOpt...)</code></a> which can be used to have the shortest code.</p>
                <p>The default color is <a href="https://docs.oracle.com/en/java/javase/25/docs/api/java.desktop/java/awt/Color.html#LIGHT_GRAY" title="class or interface in java.awt" class="external-link"><code>Color.LIGHT_GRAY</code></a>, making this useful for drawing guides.</p></br>""", element.description());


        id = "updateDataRange(java.awt.Graphics2D)";
        href = "file:///./rapaio.lib/rapaio/graphics/plot/artist/ABLine.html#" + id;
        element = parser.parseMemberDocPage(getClass().getResourceAsStream("sample.html"), href, id, JavadocType.METHOD);
        assertEquals(href, element.href());
        assertEquals(id, element.linkData());
        assertEquals(JavadocType.METHOD, element.javadocType());
        assertEquals("public void updateDataRange(Graphics2D g2d)", element.signature());
        assertEquals(2, element.searchSignatures().size());
        assertEquals("void ABLine.updateDataRange(Graphics2D g2d)", element.searchSignatures().get(0));
        assertEquals("void ABLine.updateDataRange(Graphics2D)", element.searchSignatures().get(1));
        assertEquals("""
                <dt>Specified by:</dt>
                <dd>
                 <code><a href="../Artist.html#updateDataRange(java.awt.Graphics2D)">updateDataRange</a></code>&nbsp;in class&nbsp;<code><a href="../Artist.html" title="class in rapaio.graphics.plot">Artist</a></code>
                </dd>""", element.description());
    }
}
