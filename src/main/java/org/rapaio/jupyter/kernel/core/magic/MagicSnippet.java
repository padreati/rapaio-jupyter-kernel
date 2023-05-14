package org.rapaio.jupyter.kernel.core.magic;

import java.util.List;

public record MagicSnippet(Type type, boolean oneLine, List<CodeLine> lines) {
    public record CodeLine(String code, boolean hasPosition, int relativePosition, int globalPosition) {
    }

    public CodeLine line(int index) {
        return lines.get(index);
    }

    public enum Type {
        MAGIC_ONELINE,
        MAGIC_MULTILINE,
        COMMENTS,
        EMPTY,
        NON_MAGIC
    }
}
