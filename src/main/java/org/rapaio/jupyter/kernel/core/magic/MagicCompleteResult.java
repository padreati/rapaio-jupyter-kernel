package org.rapaio.jupyter.kernel.core.magic;

import org.rapaio.jupyter.kernel.core.CompleteMatches;

public record MagicCompleteResult(boolean handled, CompleteMatches replacementOptions) {
}
