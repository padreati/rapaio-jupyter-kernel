package org.rapaio.jupyter.kernel.core.display.image;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import javax.imageio.ImageIO;

import org.rapaio.jupyter.kernel.core.Transform;
import org.rapaio.jupyter.kernel.core.display.DataRenderHandler;
import org.rapaio.jupyter.kernel.core.display.DataRendererResponse;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.MIMEType;

public class ImageHandler implements DataRenderHandler {

    @Override
    public DataRendererResponse render(Object object) {
        if (object instanceof RenderedImage image) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try {
                ImageIO.write(image, "png", Base64.getEncoder().wrap(out));
                String response = out.toString(StandardCharsets.UTF_8);

                DisplayData dd = DisplayData.withType(MIMEType.PNG, response);
                Map<String, Integer> sizeMap = Map.of("width", image.getWidth(), "height", image.getHeight());
                dd.putMetaData(MIMEType.PNG.toString(), Transform.toJson(sizeMap));
                return new DataRendererResponse(true, dd);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return DataRendererResponse.unhandled();
    }
}
