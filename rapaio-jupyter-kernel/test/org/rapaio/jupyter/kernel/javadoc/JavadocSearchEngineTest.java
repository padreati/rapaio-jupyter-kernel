package org.rapaio.jupyter.kernel.javadoc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.core.javadoc.JavadocSearchEngine;

public class JavadocSearchEngineTest {

    private static final Map<String, String> examples = new HashMap<>();

    static {
        examples.put(
                "Frame Frame.addRows(int rowCount)", """
                        Adds the following rowCount at the end of the frame. The effect depends on the implementation, for solid frames it increases rowCount, for other types it creates a bounded frame.</br><dt>Parameters:</dt>
                        <dd>
                         <code>rowCount</code> - number of rowCount to be added
                        </dd>
                        <dt>Returns:</dt>
                        <dd>new frame with rowCount appended</dd>""");

        examples.put(
                "Frame Frame.addRows(int)", """
                        Adds the following rowCount at the end of the frame. The effect depends on the implementation, for solid frames it increases rowCount, for other types it creates a bounded frame.</br><dt>Parameters:</dt>
                        <dd>
                         <code>rowCount</code> - number of rowCount to be added
                        </dd>
                        <dt>Returns:</dt>
                        <dd>new frame with rowCount appended</dd>""");

        examples.put(
                "Frame SolidFrame.bindRows(Frame df)", """
                        Builds a new frame having rows of the current frame, followed by the rows of the bounded frame. The new frame must has the same variable definitions as the current frame.</br><dt>Parameters:</dt>
                        <dd>
                         <code>df</code> - given frame with additional rows
                        </dd>
                        <dt>Returns:</dt>
                        <dd>new frame with additional rows</dd>""");

        examples.put(
                "Frame Frame.bindRows(Frame df)", """
                        Builds a new frame having rows of the current frame, followed by the rows of the bounded frame. The new frame must has the same variable definitions as the current frame.</br><dt>Parameters:</dt>
                        <dd>
                         <code>df</code> - given frame with additional rows
                        </dd>
                        <dt>Returns:</dt>
                        <dd>new frame with additional rows</dd>"""
        );

        examples.put(
                "SolidFrame SolidFrame.emptyFrom(Frame src, int rowCount)", """
                        Builds a new frame with missing values, having the same variables as in the source frame and having given row count.</br><dt>Parameters:</dt>
                        <dd>
                         <code>src</code> - source frame
                        </dd>
                        <dd>
                         <code>rowCount</code> - row count
                        </dd>
                        <dt>Returns:</dt>
                        <dd>new instance of solid frame built according with the source frame variables</dd>""");

        examples.put(
                "SolidFrame SolidFrame.emptyFrom(Frame, int)", """
                        Builds a new frame with missing values, having the same variables as in the source frame and having given row count.</br><dt>Parameters:</dt>
                        <dd>
                         <code>src</code> - source frame
                        </dd>
                        <dd>
                         <code>rowCount</code> - row count
                        </dd>
                        <dt>Returns:</dt>
                        <dd>new instance of solid frame built according with the source frame variables</dd>""");

        examples.put("SolidFrame SolidFrame.byVars(int rows, List<? extends Var> vars)", "");

        examples.put(
                "VarInt VarInt.seq(int len)", """
                        Builds an integer variable of given size as a ascending sequence starting with 0</br><dt>Parameters:</dt>
                        <dd>
                         <code>len</code> - size of the integer variable
                        </dd>
                        <dt>Returns:</dt>
                        <dd>new instance of integer variable</dd>""");

        examples.put(
                "VarInt VarInt.seq(int start, int len)", """
                        Builds an integer variable of given size as ascending sequence with a given start value</br><dt>Parameters:</dt>
                        <dd>
                         <code>start</code> - start value
                        </dd>
                        <dd>
                         <code>len</code> - size of the integer variable
                        </dd>
                        <dt>Returns:</dt>
                        <dd>new instance of integer variable</dd>""");

        examples.put(
                "VarInt VarInt.seq(int start, int len, int step)", """
                        Builds an integer variable of given size as ascending sequence with a given start value and a given step</br><dt>Parameters:</dt>
                        <dd>
                         <code>start</code> - start value
                        </dd>
                        <dd>
                         <code>len</code> - size of the index
                        </dd>
                        <dd>
                         <code>step</code> - increment value
                        </dd>
                        <dt>Returns:</dt>
                        <dd>new instance of integer variable</dd>""");

        examples.put(
                "String AbstractVar.name()", """
                        <dt>Specified by:</dt>
                        <dd>
                         <code><a href="Var.html#name()">name</a></code>&nbsp;in interface&nbsp;<code><a href="Var.html" title="interface in rapaio.data">Var</a></code>
                        </dd>
                        <dt>Returns:</dt>
                        <dd>name of the variable</dd>""");

        examples.put(
                "VarNominal VarNominal.name(String name)", """
                        Sets the variable name</br><dt>Specified by:</dt>
                        <dd>
                         <code><a href="Var.html#name(java.lang.String)">name</a></code>&nbsp;in interface&nbsp;<code><a href="Var.html" title="interface in rapaio.data">Var</a></code>
                        </dd>
                        <dt>Overrides:</dt>
                        <dd>
                         <code><a href="AbstractVar.html#name(java.lang.String)">name</a></code>&nbsp;in class&nbsp;<code><a href="AbstractVar.html" title="class in rapaio.data">AbstractVar</a></code>
                        </dd>
                        <dt>Parameters:</dt>
                        <dd>
                         <code>name</code> - future name of the variable
                        </dd>""");

        examples.put("VarNominal VarNominal.from(int rows, Function<Integer, String > func, String...dict)", "");
        examples.put("VarNominal VarNominal.from(List<String> dict, DArray<?> array)", "");
        examples.put("String Frame.head()", "");
        examples.put("String Frame.head(int)", "");
        examples.put("Frame Datasets.loadRandom(Random random)", "");

        examples.put(
                "Frame Frame.bindVars(Var...)", """
                        Adds the given variables to the variables of the current frame to build a new frame. New variables must have the same number of rows.</br><dt>Parameters:</dt>
                        <dd>
                         <code>vars</code> - variables added to the current frame variables
                        </dd>
                        <dt>Returns:</dt>
                        <dd>new frame with current frame variables and given variables added</dd>""");

        examples.put("String org.rapaio.jupyter.kernel.global.Global.display(Object o)", "");
        examples.put("String org.rapaio.jupyter.kernel.global.Global.display(String mime, Object o)", "");


        // some types: classes and interfaces

        examples.put("rapaio.graphics.plot.artist.ABLine", """
                Artist which draws a line of the form y = f(x) = a*x + b There is a generic form of the line by calling <a href="#%3Cinit%3E(double,double,rapaio.graphics.opt.GOpt...)"><code>ABLine(double, double, GOpt...)</code></a>.
                <p>Also there is a simpler form for drawing horizontal or vertical lines. The simpler form is called by using <a href="#%3Cinit%3E(boolean,double,rapaio.graphics.opt.GOpt...)"><code>ABLine(boolean, double, GOpt...)</code></a>, with the boolean parameter specifying if the line is horizontal or vertical.</p>
                <p>Also there are two dedicated shortcuts in plotter <a href="../../Plotter.html#hLine(double,rapaio.graphics.opt.GOpt...)"><code>Plotter.hLine(double, GOpt...)</code></a> and <a href="../../Plotter.html#vLine(double,rapaio.graphics.opt.GOpt...)"><code>Plotter.vLine(double, GOpt...)</code></a> which can be used to have the shortest code.</p>
                <p>The default color is <a href="https://docs.oracle.com/en/java/javase/25/docs/api/java.desktop/java/awt/Color.html#LIGHT_GRAY" title="class or interface in java.awt" class="external-link"><code>Color.LIGHT_GRAY</code></a>, making this useful for drawing guides.</p></br>""");

        examples.put("rapaio.data.Frame", """
                Random access list of observed values for multiple variables.
                <p>The observed values are represented in a tabular format. Rows corresponds to observations and columns corresponds to observed variables.</p></br>""");

        examples.put("rapaio.data.VarInt", """
                Builds a numeric variable which stores values as 32-bit integers. There are two general usage scenarios: use variable as an positive integer index or save storage for numeric variables from Z loosing decimal precision.
                <p>Missing value is <a href="https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Integer.html#MIN_VALUE" title="class or interface in java.lang" class="external-link"><code>Integer.MIN_VALUE</code></a>. Any use of this value in add/set operations will lead to missing values.</p>
                <p></p></br>""");

        examples.put("rapaio.core.tools.Grid2D", """
                """);

    }


    private JavadocSearchEngine engine;
    private File rapaioJavadocFile;
    private static final String url = "https://repo1.maven.org/maven2/io/github/padreati/rapaio-lib/8.1.0/rapaio-lib-8.1.0-javadoc.jar";

    @BeforeEach
    void beforeEach() throws IOException {
        engine = new JavadocSearchEngine();
        rapaioJavadocFile = File.createTempFile("rapaio-lib-javadoc", ".jar");
        rapaioJavadocFile.deleteOnExit();

        try (InputStream in = new BufferedInputStream(new URL(url).openStream());
             OutputStream out = new java.io.FileOutputStream(rapaioJavadocFile)) {
            final byte[] buf = new byte[64 * 1024];
            int nrBytes;
            while ((nrBytes = in.read(buf)) != -1) {
                out.write(buf, 0, nrBytes);
            }
            out.flush();
        }
    }

    @Test
    void searchTest() {
        System.out.println("Adding " + rapaioJavadocFile.getAbsolutePath());
        engine.add(rapaioJavadocFile.getAbsolutePath());

        for (var entry : examples.entrySet()) {
            String result = engine.search(entry.getKey());
            assertEquals(entry.getValue(), result, "Assertion failed for: " + entry.getKey());
        }
    }
}
