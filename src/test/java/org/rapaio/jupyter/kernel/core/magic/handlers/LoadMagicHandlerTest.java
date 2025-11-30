package org.rapaio.jupyter.kernel.core.magic.handlers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.TestUtils;
import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicParseException;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.message.Message;
import org.rapaio.jupyter.kernel.message.MessageType;
import org.rapaio.jupyter.kernel.message.messages.ShellExecuteRequest;

public class LoadMagicHandlerTest {

    private RapaioKernel kernel;
    private Channels channels;

    @BeforeEach
    void beforeEach() throws NoSuchAlgorithmException, InvalidKeyException {
        kernel = new RapaioKernel();
        channels = TestUtils.spyChannels();
        channels.connect(kernel);
    }

    @AfterEach
    void afterEach() {
        channels.close();
    }

    private static final String content1 = """
            {
             "cells": [
              {
               "cell_type": "markdown",
               "metadata": {},
               "source": [
                "This notebook is used to bootstrap rapaio library usage in jupyter notebooks. The activities included are:\\n",
                "\\n",
                "* include maved dependency on rapaio library\\n",
                "* inlcude an extensive list of imports from rapaio library to make life easier\\n",
                "* include a list of useful imports from java jdk\\n",
                "* set up printer with default values for printer (text width and image sizes)"
               ]
              },
              {
               "cell_type": "code",
               "execution_count": 1,
               "metadata": {},
               "outputs": [],
               "source": [
                "error\\n",
                "//%jars /home/ati/work/out/artifacts/rapaio_jar/rapaio.jar"
               ]
              },
              {
               "cell_type": "code",
               "execution_count": 2,
               "metadata": {},
               "outputs": [],
               "source": [
                "// repositories for beta versions\\n",
                "//%mavenRepo oss-sonatype-snapshots https://oss.sonatype.org/content/repositories/snapshots/\\n",
                "\\n",
                "// maven central\\n",
                "%maven io.github.padreati:rapaio-core:5.0.0\\n",
                "\\n",
                "// local jars"
               ]
              },
              {
               "cell_type": "code",
               "execution_count": 3,
               "metadata": {},
               "outputs": [],
               "source": [
                "// extensive list of useful imports from rapaio library\\n",
                "\\n",
                "import rapaio.core.*;\\n",
                "import rapaio.core.correlation.*;\\n",
                "import rapaio.core.distributions.*;\\n",
                "import rapaio.core.tests.*;\\n",
                "import rapaio.core.tools.*;\\n",
                "import rapaio.core.stat.*;\\n",
                "\\n",
                "import rapaio.data.*;\\n",
                "import rapaio.data.preprocessing.*;\\n",
                "import rapaio.data.group.*;\\n",
                "import rapaio.data.unique.*;\\n",
                "import rapaio.data.stream.*;\\n",
                "\\n",
                "import static rapaio.data.Group.*;\\n",
                "\\n",
                "import rapaio.datasets.*;\\n",
                "\\n",
                "import rapaio.graphics.*;\\n",
                "import rapaio.graphics.plot.*;\\n",
                "import static rapaio.graphics.Plotter.*;\\n",
                "import rapaio.graphics.opt.*;\\n",
                "\\n",
                "import rapaio.io.*;\\n",
                "\\n",
                "import static rapaio.math.MathTools.*;\\n",
                "import rapaio.math.linear.*;\\n",
                "import rapaio.math.linear.dense.*;\\n",
                "import rapaio.math.linear.decomposition.*;\\n",
                "\\n",
                "import rapaio.sys.*;\\n",
                "import static rapaio.sys.With.*;\\n",
                "import rapaio.printer.*;\\n",
                "import static rapaio.printer.Printer.*;\\n",
                "\\n",
                "import rapaio.ml.eval.*;\\n",
                "import rapaio.ml.eval.metric.*;\\n",
                "import rapaio.ml.eval.split.*;\\n",
                "import rapaio.ml.common.*;\\n",
                "import rapaio.ml.common.kernel.*;\\n",
                "\\n",
                "import rapaio.ml.model.*;\\n",
                "import rapaio.ml.model.bayes.*;\\n",
                "import rapaio.ml.model.boost.*;\\n",
                "import rapaio.ml.model.linear.*;\\n",
                "import rapaio.ml.model.rule.*;\\n",
                "import rapaio.ml.model.svm.*;\\n",
                "import rapaio.ml.model.tree.*;\\n",
                "import rapaio.ml.model.ensemble.*;\\n",
                "import rapaio.ml.model.simple.*;\\n",
                "import rapaio.ml.model.rvm.*;\\n",
                "\\n",
                "import rapaio.ml.model.km.*;\\n",
                "\\n",
                "import rapaio.util.*;\\n",
                "import rapaio.util.collection.*;\\n",
                "import rapaio.util.function.*;\\n",
                "import rapaio.util.hash.*;"
               ]
              },
              {
               "cell_type": "code",
               "execution_count": 4,
               "metadata": {},
               "outputs": [],
               "source": [
                "// list of useful imports from jdk\\n",
                "\\n",
                "import java.awt.Color;\\n",
                "import java.awt.Font;\\n",
                "\\n",
                "import java.util.function.*;\\n",
                "import java.util.stream.*;"
               ]
              },
              {
               "cell_type": "code",
               "execution_count": 5,
               "metadata": {},
               "outputs": [
                {
                 "data": {
                  "text/plain": [
                   "rapaio.printer.standard.StandardPrinter@17871113"
                  ]
                 },
                 "execution_count": 5,
                 "metadata": {},
                 "output_type": "execute_result"
                }
               ],
               "source": [
                "WS.getPrinter().withOptions(textWidth(150));\\n",
                "WS.getPrinter().withGraphicShape(800, 500);"
               ]
              },
              {
               "cell_type": "code",
               "execution_count": null,
               "metadata": {},
               "outputs": [],
               "source": []
              }
             ],
             "metadata": {
              "kernelspec": {
               "display_name": "Java",
               "language": "java",
               "name": "rapaio-jupyter-kernel"
              },
              "language_info": {
               "codemirror_mode": "java",
               "file_extension": ".jshell",
               "mimetype": "text/x-java-source",
               "name": "Java",
               "pygments_lexer": "java",
               "version": "20+36-FR"
              }
             },
             "nbformat": 4,
             "nbformat_minor": 4
            }
            """;

    @Test
    void testHandler() throws MagicEvalException, MagicParseException {
        LoadMagicHandler handler = new LoadMagicHandler();
        MagicSnippet snippet = new MagicSnippet(MagicSnippet.Type.MAGIC_LINE, List.of(new MagicSnippet.CodeLine("%load /", true, 7, 7)));
        var options = handler.snippetMagicHandlers().get(0).completeFunction().apply(null, snippet);
        assertNotNull(options);
    }

    @Test
    void loadNotebook() throws MagicEvalException, NoSuchAlgorithmException, InvalidKeyException {
        LoadMagicHandler handler = new LoadMagicHandler();
        MagicSnippet snippet = mock(MagicSnippet.class);

        handler.evalNotebook(kernel, TestUtils.context(), snippet, content1);
    }

    @Test
    void testAbsolutePath() throws NoSuchAlgorithmException, InvalidKeyException {
        kernel.handleExecuteRequest(new Message<>(MessageType.SHELL_EXECUTE_REQUEST,
                new ShellExecuteRequest("%load /home/ati/work/rapaio-notebooks/rapaio-bootstrap.ipynb", true, false, Map.of(), false,
                        false)));
    }
}
