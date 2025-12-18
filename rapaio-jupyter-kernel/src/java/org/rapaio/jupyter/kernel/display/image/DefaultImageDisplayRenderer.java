package org.rapaio.jupyter.kernel.display.image;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import javax.imageio.ImageIO;

import org.rapaio.jupyter.kernel.display.DisplayData;
import org.rapaio.jupyter.kernel.display.DisplayRenderer;
import org.rapaio.jupyter.kernel.display.MimeType;
import org.rapaio.jupyter.kernel.global.Global;

public class DefaultImageDisplayRenderer implements DisplayRenderer {

    private static final Map<MimeType, String> allowedFormats = Map.of(
            MimeType.PNG, "png",
            MimeType.GIF, "gif",
            MimeType.JPEG, "jpeg"
    );

    @Override
    public Class<?> rendererClass() {
        return RenderedImage.class;
    }

    @Override
    public boolean canRender(String mime) {
        MimeType mimeType = MimeType.from(mime, Global.config().display().defaultMimeImage());
        return allowedFormats.containsKey(mimeType);
    }

    @Override
    public DisplayData render(String mime, Object o) {
        if(!canRender(mime)) {
            throw new IllegalArgumentException("Cannot render object with this renderer.");
        }
        MimeType mimeType = MimeType.from(mime, Global.config().display().defaultMimeImage());
        if (allowedFormats.containsKey(mimeType)) {
            return renderFormat(mimeType, o);
        }
        throw new IllegalArgumentException("Cannot render object with this renderer.");
    }

    private DisplayData renderFormat(MimeType mimeType, Object o) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        RenderedImage image = (RenderedImage) o;
        try {
            ImageIO.write(image, allowedFormats.get(mimeType), Base64.getEncoder().wrap(out));
            String response = out.toString(StandardCharsets.UTF_8);

            return DisplayData.withType(mimeType.longType(), response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
