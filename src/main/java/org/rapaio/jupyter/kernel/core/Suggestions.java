package org.rapaio.jupyter.kernel.core;

import java.util.List;

public record Suggestions(List<String> replacements, int start, int end) {
}
