package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.rapaio.jupyter.kernel.core.ExecutionContext;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.SnippetMagicHandler;

public class BashMagicHandler extends MagicHandler {

    private static final Logger LOGGER = Logger.getLogger(BashMagicHandler.class.getSimpleName());

    private static final String CELL_PREFIX = "%%bash";

    @Override
    public String name() {
        return "Bash";
    }

    @Override
    public List<String> helpMessage() {
        return List.of("Executes command line in a bash shell.");
    }

    @Override
    public List<SnippetMagicHandler> snippetMagicHandlers() {
        return List.of(
                SnippetMagicHandler.cellMagic()
                        .syntaxMatcher("%%bash\\s(.+\\s*)+")
                        .syntaxPrefix("%%bash")
                        .syntaxHelp(List.of("%%bash", "bash script"))
                        .documentation(List.of("Executes the following lines in a terminal."))
                        .canHandlePredicate(this::canHandleSnippet)
                        .evalFunction(this::evalCell)
                        .build()
        );
    }

    private Object evalCell(RapaioKernel kernel, ExecutionContext context, MagicSnippet magicSnippet) throws MagicEvalException {
        if (!canHandleSnippet(magicSnippet)) {
            throw new MagicEvalException(magicSnippet, "Cannot handle the given snippet.");
        }
        String firstLine = magicSnippet.line(0).code();
        if (!firstLine.trim().equals(CELL_PREFIX)) {
            throw new MagicEvalException(magicSnippet, "Invalid command line, it should be `%%bash`",
                    0, firstLine.indexOf(CELL_PREFIX) + CELL_PREFIX.length(), firstLine.length());
        }
        List<String> lines = magicSnippet.lines().stream().skip(1).map(MagicSnippet.CodeLine::code).toList();
        List<String> commands = new ArrayList<>();

        StringBuilder lastCommand = new StringBuilder();
        for (String line : lines) {
            if (line.endsWith(" \\")) {
                lastCommand.append(line, 0, line.length() - 1);
                continue;
            }
            lastCommand.append(line);
            commands.add(lastCommand.toString());
            lastCommand = new StringBuilder();
        }
        if (!lastCommand.isEmpty()) {
            throw new MagicEvalException(magicSnippet, "Last line is not continued.", magicSnippet.lines().size() - 1, 0, magicSnippet.line(magicSnippet.lines().size() - 1).code().length());
        }

        File script = null;
        try {
            script = createTempScript(commands);
            ProcessBuilder pb = new ProcessBuilder("bash", script.toString());
            Process p = pb.start();

            // OutputStream in = p.getOutputStream();
            // no idea how to handle input
            pipe(p.getInputStream(), kernel.channels()::writeToStdOut).join();
            pipe(p.getErrorStream(), kernel.channels()::writeToStdErr).join();

            p.waitFor();

        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException((ex));
        } finally {
            if (script != null) {
                if(!script.delete()) {
                    LOGGER.severe("Scripting file %s cannot be deleted."
                            .formatted(script.getAbsolutePath()));
                }
            }
        }
        return null;
    }

    private static Thread pipe(final InputStream src, Consumer<String> consumer) {
        Thread t = new Thread(() -> {
            try {
                byte[] buffer = new byte[16 * 1024];
                while (true) {
                    int n = src.read(buffer);
                    if (n < 0) {
                        break;
                    }
                    if (n > 0) {
                        consumer.accept(new String(buffer, 0, n));
                        continue;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (IOException e) {
                // just exit
            }
        });
        t.start();
        return t;
    }

    public File createTempScript(List<String> commands) throws IOException {
        File script = File.createTempFile("script" + UUID.randomUUID(), null);
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(script)))) {
            writer.println("#!/bin/bash");
            for (var line : commands) {
                writer.println(line);
            }
        }
        return script;
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet magicSnippet) {
        return magicSnippet.isCellMagic() && magicSnippet.line(0).code().trim().startsWith(CELL_PREFIX);
    }
}
