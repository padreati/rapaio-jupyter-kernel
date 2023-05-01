package org.rapaio.jupyter.kernel.core.magic;

import org.rapaio.jupyter.kernel.core.display.DisplayData;

public record MagicInspectResult(boolean handled, DisplayData displayData) {
}
