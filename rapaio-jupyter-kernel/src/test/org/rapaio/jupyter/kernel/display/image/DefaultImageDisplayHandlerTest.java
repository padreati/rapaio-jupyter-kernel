package org.rapaio.jupyter.kernel.display.image;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.display.DisplayData;
import org.rapaio.jupyter.kernel.display.MimeType;
import org.rapaio.jupyter.kernel.display.Display;

public class DefaultImageDisplayHandlerTest {

    @Test
    void testImageRender() throws IOException {
        BufferedImage image = ImageIO.read(URI.create("http://www.xmpie.com/VDPSoftware/assets/images/untitled-11-1046x547.png").toURL());
        DisplayData dd = Display.inst().render(image);
        assertNotNull(dd);
        assertTrue(dd.data().containsKey(MimeType.PNG.toString()));
    }
}
