package org.rapaio.jupyter.kernel.core.magic;

import org.rapaio.jupyter.kernel.core.Suggestions;

public record MagicCompleteResult(boolean handled, Suggestions replacementOptions) {
}
