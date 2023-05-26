package org.rapaio.jupyter.kernel.core.magic.handlers;

import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.magic.*;

import java.io.File;
import java.util.List;

public class ClasspathMagicHandler extends MagicHandler {

    private static final String ONE_LINE_PREFIX = "%classpath ";

    @Override
    public String name() {
        return "Classpath";
    }

    @Override
    public List<String> helpMessage() {
        return List.of("Magic handler which allows one to add to classpath directories with compiled code.");
    }

    @Override
    public List<LineMagicHandler> oneLineMagicHandlers() {
        return List.of(
                LineMagicHandler.builder()
                        .syntaxPrefix("%classpath ")
                        .syntaxMatcher("%classpath .*")
                        .syntaxHelp("%classpath path_to_folder_with_classes")
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

    private Object evalLine(RapaioKernel kernel, MagicSnippet magicSnippet) throws MagicParseException, MagicEvalException {
        if (!canHandleSnippet(magicSnippet)) {
            throw new MagicParseException(name(), magicSnippet, "Snippet cannot be handled by this magic handler.");
        }
        String fullCode = magicSnippet.lines().get(0).code();
        String path = fullCode.substring(ONE_LINE_PREFIX.length()).trim();

        File file = new File(path);
        if (!file.exists()) {
            throw new MagicEvalException(magicSnippet, "Provided path does not exist.");
        }
        if (!file.isDirectory()) {
            throw new MagicEvalException(magicSnippet, "Provided path is not a directory.");
        }
        kernel.javaEngine().getShell().addToClasspath(file.getAbsolutePath());
        kernel.channels().writeToStdOut(ANSI.start().fgGreen().text("Add " + file.getAbsolutePath() + " to classpath\n").render());
        return null;
    }

    private CompleteMatches completeLine(RapaioKernel kernel, MagicSnippet magicSnippet) {
        return HandlerUtils.oneLinePathComplete(ONE_LINE_PREFIX, magicSnippet, File::isDirectory);
    }
}
