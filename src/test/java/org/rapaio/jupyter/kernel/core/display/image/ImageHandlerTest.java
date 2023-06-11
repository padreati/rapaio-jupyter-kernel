package org.rapaio.jupyter.kernel.core.display.image;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.MIMEType;
import org.rapaio.jupyter.kernel.core.display.Renderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageHandlerTest {

    @Test
    void testImageRender() throws IOException {
        BufferedImage image = ImageIO.read(URI.create("https://www.xmpie.com/VDPSoftware/assets/images/untitled-11-1046x547.png").toURL());
        DisplayData dd = new Renderer().render(image);
        assertNotNull(dd);
        assertTrue(dd.data().containsKey(MIMEType.PNG.toString()));
    }
}
