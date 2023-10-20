package org.rapaio.jupyter.kernel.core.display.image;

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

import org.rapaio.jupyter.kernel.core.display.DataRenderHandler;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.MIMEType;

public class ImageRenderer implements DataRenderHandler {

    private static final Set<String> availableMimeTypes = new HashSet<>(List.of(ImageIO.getWriterMIMETypes()));
    private static final Map<MIMEType, String> allowedFormats = Map.of(
            MIMEType.PNG, "png",
            MIMEType.GIF, "gif",
            MIMEType.JPEG, "jpeg"
    );

    @Override
    public boolean canRender(Object o) {
        return o instanceof RenderedImage;
    }

    @Override
    public MIMEType defaultMIMEType() {
        return MIMEType.PNG;
    }

    @Override
    public DisplayData render(MIMEType mimeType, Object o) {
        if (availableMimeTypes.contains(mimeType.toString()) && allowedFormats.containsKey(mimeType)) {
            return renderFormat(mimeType, o);
        }
        return renderFormat(defaultMIMEType(), o);
    }

    private DisplayData renderFormat(MIMEType mimeType, Object o) {

        if (!canRender(o)) {
            throw new IllegalArgumentException("Cannot render object with this renderer.");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        RenderedImage image = (RenderedImage) o;
        try {
            ImageIO.write(image, allowedFormats.get(mimeType), Base64.getEncoder().wrap(out));
            String response = out.toString(StandardCharsets.UTF_8);

            return DisplayData.withType(mimeType, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
