package org.rapaio.jupyter.kernel.extension;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.MIMEType;
import org.rapaio.jupyter.kernel.core.display.Renderer;

import static org.junit.jupiter.api.Assertions.*;

public class JupyterDisplayableTest {

    @Test
    void smokeTest() {
        var obj = new JupyterDisplayable() {

            @Override
            public MIMEType defaultMIMEType() {
                return MIMEType.TEXT;
            }

            @Override
            public DisplayData render(MIMEType mimeType) {
                MIMEType callMimeType =  (mimeType != MIMEType.HTML) ? defaultMIMEType() : mimeType;
                String data = callMimeType.toString();
                return DisplayData.withType(callMimeType, data);
            }
        };

        Renderer renderer = new Renderer();

        var defaultDisplay = renderer.render(obj);
        assertNotNull(defaultDisplay);
        assertNotNull(defaultDisplay.data());
        assertTrue(defaultDisplay.data().containsKey("text/plain"));
        assertEquals("text/plain", defaultDisplay.data().get("text/plain"));

        var implementedDisplay = renderer.render(MIMEType.HTML, obj);
        assertNotNull(implementedDisplay);
        assertNotNull(implementedDisplay.data());
        assertTrue(implementedDisplay.data().containsKey("text/html"));
        assertEquals("text/html", implementedDisplay.data().get("text/html"));

        var notImplementedDisplay = renderer.render(MIMEType.JPEG, obj);
        assertNotNull(notImplementedDisplay);
        assertNotNull(notImplementedDisplay.data());
        assertTrue(notImplementedDisplay.data().containsKey("text/plain"));
        assertEquals("text/plain", notImplementedDisplay.data().get("text/plain"));
    }
}
