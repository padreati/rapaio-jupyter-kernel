package org.rapaio.jupyter.kernel.display.image;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.rapaio.jupyter.kernel.display.DisplayData;
import org.rapaio.jupyter.kernel.display.DisplayHandler;
import org.rapaio.jupyter.kernel.display.MimeType;

public class ImageDisplayHandler implements DisplayHandler {

    private static final Set<String> availableMimeTypes = new HashSet<>(List.of(ImageIO.getWriterMIMETypes()));
    private static final Map<MimeType, String> allowedFormats = Map.of(
            MimeType.PNG, "png",
            MimeType.GIF, "gif",
            MimeType.JPEG, "jpeg"
    );

    @Override
    public boolean canRender(Object o) {
        return o instanceof RenderedImage;
    }

    @Override
    public String defaultMIMEType() {
        return MimeType.PNG.toString();
    }

    @Override
    public DisplayData render(String mimeType, Object o) {
        if (availableMimeTypes.contains(mimeType) && allowedFormats.containsKey(mimeType)) {
            return renderFormat(mimeType, o);
        }
        return renderFormat(defaultMIMEType(), o);
    }

    private DisplayData renderFormat(String mime, Object o) {

        if (!canRender(o)) {
            throw new IllegalArgumentException("Cannot render object with this renderer.");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        RenderedImage image = (RenderedImage) o;
        try {
            ImageIO.write(image, allowedFormats.get(MimeType.fromString(mime)), Base64.getEncoder().wrap(out));
            String response = out.toString(StandardCharsets.UTF_8);

            return DisplayData.withType(mime, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
