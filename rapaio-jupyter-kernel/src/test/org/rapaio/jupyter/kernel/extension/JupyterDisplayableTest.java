package org.rapaio.jupyter.kernel.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.display.DisplaySystem;
import org.rapaio.jupyter.kernel.display.MimeType;
import org.rapaio.jupyter.kernel.global.Global;

public class JupyterDisplayableTest {

    @Test
    void smokeTest() {
        var obj = "text";

        var defaultMime = Global.config().display().defaultMime();
        var defaultDisplay = DisplaySystem.inst().render(obj);
        assertNotNull(defaultDisplay);
        assertNotNull(defaultDisplay.data());
        assertTrue(defaultDisplay.data().containsKey(defaultMime));
        assertEquals("text", defaultDisplay.data().get(defaultMime));

        var implementedDisplay = DisplaySystem.inst().render(MimeType.HTML.toString(), obj);
        assertNotNull(implementedDisplay);
        assertNotNull(implementedDisplay.data());
        assertTrue(implementedDisplay.data().containsKey("text/html"));
        assertEquals("text", implementedDisplay.data().get("text/html"));

        var notImplementedDisplay = DisplaySystem.inst().render(MimeType.TEXT.toString(), obj);
        assertNotNull(notImplementedDisplay);
        assertNotNull(notImplementedDisplay.data());
        assertTrue(notImplementedDisplay.data().containsKey("text/plain"));
        assertEquals("text", notImplementedDisplay.data().get("text/plain"));
    }
}
