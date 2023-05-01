package org.rapaio.jupyter.kernel.core.magic;

import java.util.List;

public record MagicSnippet(boolean oneLine, List<CodeLine> lines) {
    public record CodeLine(String code, boolean hasPosition, int relativePosition, int globalPosition) {
    }
}
