package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.io.File;
import java.util.List;

import org.rapaio.jupyter.kernel.channels.ReplyEnv;
import org.rapaio.jupyter.kernel.core.ReplacementOptions;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicEvaluator;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicParseException;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;

public class JarMagicHandler implements MagicHandler {

    private static final String PREFIX = "%jar ";

    @Override
    public String name() {
        return "Jar magic handler";
    }

    @Override
    public String syntax() {
        return PREFIX + "path_to_jar_or_folder_of_jars";
    }

    @Override
    public List<String> helpMessage() {
        return List.of(
                "Jar magic handler allows adding to the current classpath a local jar file or all "
                        + "the jar files from a given directory."
        );
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet snippet) {
        return snippet.oneLine() && snippet.lines().size() == 1 && snippet.lines().get(0).code().startsWith(PREFIX);
    }

    @Override
    public Object eval(MagicEvaluator magicEvaluator, JavaEngine engine, ReplyEnv env, MagicSnippet snippet) throws MagicParseException,
            MagicEvalException {
        if (!canHandleSnippet(snippet)) {
            throw new IllegalArgumentException("Snippet cannot be handled by this magic handler.");
        }
        String fullCode = snippet.lines().get(0).code();
        String path = fullCode.substring(PREFIX.length()).trim();

        File file = new File(path);
        if (!file.exists()) {
            throw new MagicEvalException(snippet, "Provided path does not exist.");
        }
        if (file.isDirectory()) {
            File[] jars = file.listFiles(f -> f.getName().endsWith(".jar"));
            if (jars == null) {
                env.writeToStdOut(ANSI.start().bold().fgGreen().text("No jar files were found.\n").build());
                return null;
            }
            env.writeToStdOut(ANSI.start().fgGreen().text("Found " + jars.length + " jar files.\n").build());
            for (File jar : jars) {
                engine.getShell().addToClasspath(jar.getAbsolutePath());
                env.writeToStdOut(ANSI.start().fgGreen().text("Add " + jar.getAbsolutePath() + " to classpath\n").build());
            }
        } else {
            if (!file.getName().endsWith(".jar")) {
                throw new MagicEvalException(snippet, "Provided input is not a jar file.");
            }
            engine.getShell().addToClasspath(file.getAbsolutePath());
            env.writeToStdOut(ANSI.start().fgGreen().text("Add " + file.getAbsolutePath() + " to classpath").build());
        }
        return null;
    }

    @Override
    public ReplacementOptions complete(ReplyEnv env, MagicSnippet snippet) {
        return HandlerUtils.oneLinePathComplete(PREFIX, snippet,
                f -> (f.isDirectory() || f.getName().endsWith(".jar")));
    }
}
