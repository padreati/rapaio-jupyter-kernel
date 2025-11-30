package org.rapaio.jupyter.kernel.core.magic;

import java.util.List;

public record MagicSnippet(Type type, List<CodeLine> lines) {
    public record CodeLine(String code, boolean hasPosition, int relativePosition, int globalPosition) {
    }

    public CodeLine line(int index) {
        return lines.get(index);
    }

    public enum Type {
        MAGIC_LINE,
        MAGIC_CELL,
        NON_MAGIC
    }

    public boolean isLineMagic() {
        return type ==Type.MAGIC_LINE;
    }

    public boolean isCellMagic() {
        return type == Type.MAGIC_CELL;
    }
}
