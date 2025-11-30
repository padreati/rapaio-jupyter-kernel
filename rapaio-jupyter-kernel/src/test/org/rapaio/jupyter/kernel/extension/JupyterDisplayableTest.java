package org.rapaio.jupyter.kernel.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.display.DisplayData;
import org.rapaio.jupyter.kernel.display.Displayable;
import org.rapaio.jupyter.kernel.display.MIMEType;
import org.rapaio.jupyter.kernel.display.Renderer;

public class JupyterDisplayableTest {

    @Test
    void smokeTest() {
        var obj = new Displayable() {

            @Override
            public String defaultMIME() {
                return "text/plain";
            }

            @Override
            public DisplayData render(String mimeType) {
                String callMimeType =  (mimeType != MIMEType.HTML.toString()) ? defaultMIME() : mimeType;
                return DisplayData.withType(callMimeType, callMimeType);
            }
        };

        Renderer renderer = new Renderer();

        var defaultDisplay = renderer.render(obj);
        assertNotNull(defaultDisplay);
        assertNotNull(defaultDisplay.data());
        assertTrue(defaultDisplay.data().containsKey("text/plain"));
        assertEquals("text/plain", defaultDisplay.data().get("text/plain"));

        var implementedDisplay = renderer.render(MIMEType.HTML.toString(), obj);
        assertNotNull(implementedDisplay);
        assertNotNull(implementedDisplay.data());
        assertTrue(implementedDisplay.data().containsKey("text/html"));
        assertEquals("text/html", implementedDisplay.data().get("text/html"));

        var notImplementedDisplay = renderer.render(MIMEType.JPEG.toString(), obj);
        assertNotNull(notImplementedDisplay);
        assertNotNull(notImplementedDisplay.data());
        assertTrue(notImplementedDisplay.data().containsKey("text/plain"));
        assertEquals("text/plain", notImplementedDisplay.data().get("text/plain"));
    }
}
