package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.io.File;
import java.util.List;

import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicParseException;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.OneLineMagicHandler;

public class JarMagicHandler implements MagicHandler {

    private static final String PREFIX = "%jar ";

    @Override
    public String name() {
        return "Jar magic handler";
    }

    @Override
    public List<OneLineMagicHandler> oneLineMagicHandlers() {
        return List.of(
                OneLineMagicHandler.builder()
                        .syntaxMatcher("%jar .*")
                        .syntaxHelp("%jar path_to_jar_or_folder_of_jars")
                        .documentation(List.of(
                                "Adds to the classpath a jar or all jar archives from a directory"
                        ))
                        .canHandlePredicate(this::canHandleSnippet)
                        .evalFunction(this::evalLine)
                        .inspectFunction((channels, magicSnippet) -> null)
                        .completeFunction(this::complete)
                        .build()
        );
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

    public Object evalLine(MagicEngine magicEngine, JavaEngine engine, Channels channels, MagicSnippet snippet) throws MagicParseException,
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
                channels.writeToStdOut(ANSI.start().bold().fgGreen().text("No jar files were found.\n").build());
                return null;
            }
            channels.writeToStdOut(ANSI.start().fgGreen().text("Found " + jars.length + " jar files.\n").build());
            for (File jar : jars) {
                engine.getShell().addToClasspath(jar.getAbsolutePath());
                channels.writeToStdOut(ANSI.start().fgGreen().text("Add " + jar.getAbsolutePath() + " to classpath\n").build());
            }
        } else {
            if (!file.getName().endsWith(".jar")) {
                throw new MagicEvalException(snippet, "Provided input is not a jar file.");
            }
            engine.getShell().addToClasspath(file.getAbsolutePath());
            channels.writeToStdOut(ANSI.start().fgGreen().text("Add " + file.getAbsolutePath() + " to classpath").build());
        }
        return null;
    }

    @Override
    public CompleteMatches complete(Channels channels, MagicSnippet snippet) {
        return HandlerUtils.oneLinePathComplete(PREFIX, snippet,
                f -> (f.isDirectory() || f.getName().endsWith(".jar")));
    }
}
