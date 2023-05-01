package org.rapaio.jupyter.kernel.core.magic;

import org.rapaio.jupyter.kernel.core.ReplacementOptions;

public record MagicCompleteResult(boolean handled, ReplacementOptions replacementOptions) {
}
