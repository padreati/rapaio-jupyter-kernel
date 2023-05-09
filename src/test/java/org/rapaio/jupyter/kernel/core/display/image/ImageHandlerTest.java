package org.rapaio.jupyter.kernel.core.display.image;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.MIMEType;
import org.rapaio.jupyter.kernel.core.display.Renderer;

public class ImageHandlerTest {

    @Test
    void testImageRender() throws IOException {
        BufferedImage image = ImageIO.read(URI.create("https://www.xmpie.com/VDPSoftware/assets/images/untitled-11-1046x547.png").toURL());
        DisplayData dd = new Renderer().render(image);
        assertNotNull(dd);
        assertTrue(dd.data().containsKey(MIMEType.PNG.toString()));
        assertTrue(dd.metadata().containsKey(MIMEType.PNG.toString()));
    }
}
