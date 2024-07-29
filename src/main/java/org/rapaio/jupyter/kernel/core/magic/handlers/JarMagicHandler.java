package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.ExecutionContext;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicHandlerTools;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.SnippetMagicHandler;

public class JarMagicHandler extends MagicHandler {

    private static final String LINE_PREFIX = "%jar";
    private static final String CELL_PREFIX = "%%jars";

    @Override
    public String name() {
        return "Jar magic handler";
    }

    @Override
    public List<SnippetMagicHandler> snippetMagicHandlers() {
        return List.of(
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher("%jar .*")
                        .syntaxHelp(List.of("%jar path_to_jar_or_folder_of_jars"))
                        .syntaxPrefix(LINE_PREFIX)
                        .documentation(List.of(
                                "Adds to the classpath a jar or all jar archives from a directory"
                        ))
                        .canHandlePredicate(this::canHandleLine)
                        .evalFunction(this::evalLine)
                        .completeFunction(this::completeLine)
                        .build(),
                SnippetMagicHandler.cellMagic()
                        .syntaxMatcher("%%jars\\s(.+\\s*)+")
                        .syntaxHelp(List.of(
                                "%%jars",
                                "jar_file_or_folder_1",
                                "[jar_file_or_folder_2]",
                                "...",
                                "[jar_file_or_folder_n]"))
                        .syntaxPrefix(CELL_PREFIX)
                        .documentation(List.of(
                                "Adds to classpath all referenced jars or all jar files from referenced directories."
                        ))
                        .canHandlePredicate(this::canHandleCell)
                        .evalFunction(this::evalCell)
                        .completeFunction(this::completeCell)
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
    public boolean canHandleSnippet(MagicSnippet magicSnippet) {
        return canHandleLine(magicSnippet) || canHandleCell(magicSnippet);
    }

    private boolean canHandleLine(MagicSnippet magicSnippet) {
        return magicSnippet.isLineMagic() && magicSnippet.line(0).code().startsWith(LINE_PREFIX);
    }

    private boolean canHandleCell(MagicSnippet magicSnippet) {
        return magicSnippet.isCellMagic() && magicSnippet.line(0).code().startsWith(CELL_PREFIX);
    }

    private Object evalLine(RapaioKernel kernel, MagicSnippet magicSnippet) throws MagicEvalException {
        if (!canHandleSnippet(magicSnippet)) {
            throw new MagicEvalException(magicSnippet, "Snippet cannot be handled by this magic handler.");
        }
        String fullCode = magicSnippet.lines().get(0).code();
        String path = fullCode.substring(LINE_PREFIX.length() + 1).trim();
        path = kernel.executionContext().getRelativePath(Path.of(path)).toAbsolutePath().toString();

        File file = new File(path);
        if (!file.getAbsoluteFile().exists()) {
            throw new MagicEvalException(magicSnippet, "Provided path does not exist:" + file.getAbsolutePath());
        }
        if (!file.getAbsoluteFile().getName().endsWith(".jar")) {
            throw new MagicEvalException(magicSnippet, "Provided input is not a jar file.");
        }
        addFileToPath(kernel, file);
        return null;
    }

    private Object evalCell(RapaioKernel kernel, MagicSnippet magicSnippet) throws MagicEvalException {
        if (!canHandleCell(magicSnippet)) {
            throw new MagicEvalException(magicSnippet, "Snippet cannot be handled by this magic handler.");
        }

        // test first line is only the command
        var code = magicSnippet.line(0).code();
        if (!code.trim().equals(CELL_PREFIX)) {
            throw new MagicEvalException(magicSnippet, "Invalid command line, it should be `%%jars`",
                    0, code.indexOf(CELL_PREFIX) + CELL_PREFIX.length(), code.length());
        }

        // all other lines should be added

        List<File> files = new ArrayList<>();
        for (int i = 1; i < magicSnippet.lines().size(); i++) {
            files.add(getFile(kernel.executionContext(), magicSnippet, i));
        }
        files.forEach(file -> addFileToPath(kernel, file));
        return null;
    }

    private static File getFile(ExecutionContext context, MagicSnippet magicSnippet, int i) throws MagicEvalException {
        String path = magicSnippet.lines().get(i).code();
        path = context.getRelativePath(Path.of(path)).toAbsolutePath().toString();
        File file = new File(path.trim());
        if (!file.getAbsoluteFile().exists()) {
            throw new MagicEvalException(magicSnippet, "Provided path does not exist: " + file.getAbsolutePath(), i, 0,
                    path.length());
        }
        if (!file.isDirectory() && !file.getName().endsWith(".jar")) {
            throw new MagicEvalException(magicSnippet, "Provided input is not a jar file.", i, 0, path.length());
        }
        return file;
    }

    private void addFileToPath(RapaioKernel kernel, File file) {
        if (file.isDirectory()) {
            File[] jars = file.listFiles(f -> f.getName().endsWith(".jar"));
            if (jars == null) {
                kernel.channels().writeToStdOut(ANSI.start().bold().fgGreen().text("No jar files were found.\n").render());
                return;
            }
            kernel.channels().writeToStdOut(ANSI.start().fgGreen().text("Found " + jars.length + " jar files.\n").render());
            for (File jar : jars) {
                kernel.javaEngine().getShell().addToClasspath(jar.getAbsolutePath());
                kernel.channels().writeToStdOut(ANSI.start().fgGreen().text("Add " + jar.getAbsolutePath() + " to classpath\n").render());
            }
        } else {
            kernel.javaEngine().getShell().addToClasspath(file.getAbsolutePath());
            kernel.channels().writeToStdOut(ANSI.start().fgGreen().text("Add " + file.getAbsolutePath() + " to classpath").render());
        }
    }

    private CompleteMatches completeLine(RapaioKernel kernel, MagicSnippet magicSnippet) {
        return MagicHandlerTools.oneLinePathComplete(LINE_PREFIX, magicSnippet,
                f -> (f.isDirectory() || f.getName().endsWith(".jar")));
    }

    private CompleteMatches completeCell(RapaioKernel kernel, MagicSnippet snippet) {
        FileFilter fileFilter = f -> (f.isDirectory() || f.getName().endsWith(".jar"));
        for (int i = 0; i < snippet.lines().size(); i++) {
            var line = snippet.line(i);
            if (!line.hasPosition()) {
                continue;
            }
            if (i == 0) {
                return completeLine(kernel, snippet);
            }
            String code = line.code();
            String path = code.substring(0, line.relativePosition());

            int indexPos = path.lastIndexOf('/') + 1;
            String pathPrefix = path.substring(indexPos);
            path = path.substring(0, indexPos);
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                File[] children = file.listFiles(f -> f.getName().startsWith(pathPrefix) && (fileFilter.accept(f)));
                if (children != null) {
                    List<String> options =
                            new ArrayList<>(Arrays.stream(children).map(f -> f.isDirectory() ? f.getName() + '/' : f.getName()).toList());
                    options.sort(String::compareTo);
                    return new CompleteMatches(options,
                            line.globalPosition() - line.relativePosition() + path.length(),
                            line.globalPosition() - line.relativePosition() + line.code().length());
                }
            }
        }
        return null;
    }
}
