package org.rapaio.jupyter.display.provider;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.display.DisplaySystem;

public class DisplaySystemTest {

    @Test
    void smokeTest() {
        DisplaySystem.inst().refreshSpiDisplayHandlers();

        DisplaySystem.inst().render(Map.of());
    }
}
