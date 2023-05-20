package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.Transform;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.MIMEType;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.OneLineMagicHandler;

public class ImageMagicHandler extends MagicHandler {

    private static final String PREFIX = "%image ";

    @Override
    public String name() {
        return "Image";
    }

    @Override
    public List<String> helpMessage() {
        return List.of("Displays an image from a file or URL.");
    }

    @Override
    public List<OneLineMagicHandler> oneLineMagicHandlers() {
        return List.of(
                OneLineMagicHandler.builder()
                        .syntaxMatcher("%image .*")
                        .syntaxHelp("%image path_to_file_or_url")
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
        return magicSnippet.oneLine() && magicSnippet.lines().size() == 1 && magicSnippet.lines().get(0).code().startsWith(PREFIX);
    }

    Object evalLine(RapaioKernel kernel, MagicSnippet magicSnippet) throws MagicEvalException {
        if (!canHandleSnippet(magicSnippet)) {
            throw new MagicEvalException(magicSnippet, "Cannot handle an unmatched snippet.");
        }
        MagicSnippet.CodeLine line = magicSnippet.line(0);
        String src = line.code().trim().substring(PREFIX.length());

        File file = new File(src);

        try {
            URI uri = file.exists() ? file.toURI() : URI.create(src);
            var bi = ImageIO.read(uri.toURL().openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            ImageIO.write(bi, "png", Base64.getEncoder().wrap(out));
            String response = out.toString(StandardCharsets.UTF_8);

            DisplayData dd = DisplayData.withType(MIMEType.PNG, response);
            Map<String, Integer> sizeMap = Map.of("width", bi.getWidth(), "height", bi.getHeight());
            dd.putMetaData(MIMEType.PNG.toString(), Transform.toJson(sizeMap));
            return dd;
        } catch (IOException e) {
            throw new MagicEvalException(magicSnippet, "Could not read image. " + e.getMessage());
        }
    }

    CompleteMatches completeLine(RapaioKernel kernel, MagicSnippet snippet) {
        Set<String> fileSuffixes = Arrays.stream(ImageIO.getReaderFileSuffixes()).map(String::toLowerCase).collect(Collectors.toSet());
        return HandlerUtils.oneLinePathComplete(PREFIX, snippet,
                f -> f.isDirectory() || fileSuffixes.contains(f.getName().substring(f.getName().lastIndexOf('.') + 1).toLowerCase()));
    }
}
