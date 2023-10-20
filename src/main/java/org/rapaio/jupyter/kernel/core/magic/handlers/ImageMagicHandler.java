package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.ExecutionContext;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.MIMEType;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicHandlerTools;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.SnippetMagicHandler;

public class ImageMagicHandler extends MagicHandler {

    private static final String PREFIX = "%image";

    @Override
    public String name() {
        return "Image";
    }

    @Override
    public List<String> helpMessage() {
        return List.of("Displays an image from a file or URL.");
    }

    @Override
    public List<SnippetMagicHandler> snippetMagicHandlers() {
        return List.of(
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher("%image .*")
                        .syntaxHelp(List.of("%image path_to_file_or_url"))
                        .syntaxPrefix("%image ")
                        .documentation(List.of("Display an image from a local file or from an URL"))
                        .canHandlePredicate(this::canHandleSnippet)
                        .evalFunction(this::evalLine)
                        .completeFunction(this::completeLine)
                        .build()
        );
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet magicSnippet) {
        return canHandleOneLinePrefix(magicSnippet, PREFIX);
    }

    Object evalLine(RapaioKernel kernel, ExecutionContext context, MagicSnippet magicSnippet) throws MagicEvalException {
        if (!canHandleSnippet(magicSnippet)) {
            throw new MagicEvalException(magicSnippet, "Cannot handle an unmatched snippet.");
        }
        MagicSnippet.CodeLine line = magicSnippet.line(0);
        String src = line.code().trim().substring(PREFIX.length() + 1);

        File file = new File(src);

        try {
            URI uri = file.exists() ? file.toURI() : URI.create(src);
            var bi = ImageIO.read(uri.toURL().openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            ImageIO.write(bi, "png", Base64.getEncoder().wrap(out));
            String response = out.toString(StandardCharsets.UTF_8);

            return DisplayData.withType(MIMEType.PNG, response);
        } catch (IOException e) {
            throw new MagicEvalException(magicSnippet, "Could not read image. " + e.getMessage());
        }
    }

    CompleteMatches completeLine(RapaioKernel kernel, ExecutionContext context, MagicSnippet snippet) {
        Set<String> fileSuffixes = Arrays.stream(ImageIO.getReaderFileSuffixes()).map(String::toLowerCase).collect(Collectors.toSet());
        return MagicHandlerTools.oneLinePathComplete(PREFIX, snippet,
                f -> f.isDirectory() || fileSuffixes.contains(f.getName().substring(f.getName().lastIndexOf('.') + 1).toLowerCase()));
    }
}
