package org.rapaio.jupyter.kernel.core;

import java.util.List;

public record ReplacementOptions(List<String> replacements, int start, int end) {
}
