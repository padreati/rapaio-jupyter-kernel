package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.io.File;
import java.nio.file.Path;
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

public class ClasspathMagicHandler extends MagicHandler {

    private static final String ONE_LINE_PREFIX = "%classpath";

    @Override
    public String name() {
        return "Classpath";
    }

    @Override
    public List<String> helpMessage() {
        return List.of("Magic handler which allows one to add to classpath directories with compiled code.");
    }

    @Override
    public List<SnippetMagicHandler> snippetMagicHandlers() {
        return List.of(
                SnippetMagicHandler.lineMagic()
                        .syntaxPrefix("%classpath ")
                        .syntaxMatcher("%classpath .*")
                        .syntaxHelp(List.of("%classpath path_to_folder_with_classes"))
                        .documentation(List.of("Adds to classpath folder which contains class files"))
                        .canHandlePredicate(this::canHandleSnippet)
                        .evalFunction(this::evalLine)
                        .completeFunction(this::completeLine)
                        .build()
        );
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet magicSnippet) {
        return canHandleOneLinePrefix(magicSnippet, ONE_LINE_PREFIX);
    }

    private Object evalLine(RapaioKernel kernel, MagicSnippet magicSnippet) throws MagicEvalException {
        if (!canHandleSnippet(magicSnippet)) {
            throw new MagicEvalException(magicSnippet, "Snippet cannot be handled by this magic handler.");
        }
        File file = getFile(kernel.getExecutionContext(), magicSnippet);
        kernel.javaEngine().getShell().addToClasspath(file.getAbsolutePath());
        kernel.channels().writeToStdOut(ANSI.start().fgGreen().text("Add " + file.getAbsolutePath() + " to classpath\n").render());
        return null;
    }

    private static File getFile(ExecutionContext context, MagicSnippet magicSnippet) throws MagicEvalException {
        String fullCode = magicSnippet.lines().getFirst().code();
        String path = fullCode.substring(ONE_LINE_PREFIX.length()).trim();
        path = context.getRelativePath(Path.of(path)).toAbsolutePath().toString();

        File file = new File(path);
        if (!file.exists()) {
            throw new MagicEvalException(magicSnippet, "Provided path does not exist.");
        }
        if (!file.isDirectory()) {
            throw new MagicEvalException(magicSnippet, "Provided path is not a directory.");
        }
        return file;
    }

    private CompleteMatches completeLine(RapaioKernel kernel, MagicSnippet magicSnippet) {
        return MagicHandlerTools.oneLinePathComplete(ONE_LINE_PREFIX, magicSnippet, File::isDirectory);
    }
}
