package org.rapaio.jupyter.kernel.core.magic;

import org.rapaio.jupyter.kernel.display.DisplayData;

public record MagicInspectResult(boolean handled, DisplayData displayData) {
}
