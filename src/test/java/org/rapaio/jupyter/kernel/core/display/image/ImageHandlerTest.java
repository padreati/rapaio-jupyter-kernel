package org.rapaio.jupyter.kernel.core.display.image;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.core.display.DefaultRenderer;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.MIMEType;

public class ImageHandlerTest {

    @Test
    void testImageRender() throws IOException {
        BufferedImage image = ImageIO.read(new URL("https://www.xmpie.com/VDPSoftware/assets/images/untitled-11-1046x547.png"));
        DisplayData dd = new DefaultRenderer().render(image);
        assertNotNull(dd);
        assertTrue(dd.data().containsKey(MIMEType.PNG.toString()));
        assertTrue(dd.metadata().containsKey(MIMEType.PNG.toString()));
    }
}
