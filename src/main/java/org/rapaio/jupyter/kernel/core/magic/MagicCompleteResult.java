package org.rapaio.jupyter.kernel.core.magic;

import org.rapaio.jupyter.kernel.core.Replacements;

public record MagicCompleteResult(boolean handled, Replacements replacementOptions) {
}
