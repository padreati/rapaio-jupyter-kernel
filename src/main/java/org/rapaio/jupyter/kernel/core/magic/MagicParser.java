package org.rapaio.jupyter.kernel.core.magic;

import java.util.ArrayList;
import java.util.List;

public class MagicParser {

    /**
     * Parse source into a list of {@link MagicSnippet}. A magic snippet is a line if the snippet starts with
     * character '%' or a multiline if the magic starts with '%%'. The token next to '%' or '%%' is not checked,
     * since those starting characters
     * are not valid java starting chars and the intent is clear that it should contain a magic code.
     * <p>
     * The parsing produces multiples types of snippets given by {@link MagicSnippet.Type}.
     * <p>
     * If the list of magic snippets will contain only snippets with types {@link MagicSnippet.Type#NON_MAGIC},
     * than the intent of the code is recognized as not directed to magic engine, and consequently
     * deferred to Java engine for interpretation.
     * If the resulting snippets contains at least one {@link MagicSnippet.Type#NON_MAGIC} and at least one of
     * type {@link MagicSnippet.Type#MAGIC_LINE} or {@link MagicSnippet.Type#MAGIC_CELL} than an erroneous
     * situation is considered.
     * <p>
     * A valid magic code should contain any types other than {@link MagicSnippet.Type#NON_MAGIC}.
     *
     * @param sourceCode the original source code of the cell
     * @param position   the position in code of the cursor
     * @return a list of parsed magic snippets
     */
    public List<MagicSnippet> parseSnippets(String sourceCode, int position) {
        String[] lines = sourceCode.split("\\R");

        List<MagicSnippet> snippets = new ArrayList<>();

        boolean cellMagic = false;
        List<MagicSnippet.CodeLine> cellLines = null;

        int start = 0;
        for (String line : lines) {

            int end = start + line.length();
            boolean hasPosition = position >= start && position <= end;
            int relativePosition = (position >= start && position <= end) ? position - start : -1;
            start += line.length() + 1;

            // skip empty lines and comments
            if (isEmpty(line) || hasComment(line)) {
                continue;
            }

            // found a line magic
            if (hasLineMarker(line)) {
                if (cellLines != null) {
                    // collect accumulation
                    snippets.add(new MagicSnippet(cellMagic ? MagicSnippet.Type.MAGIC_CELL : MagicSnippet.Type.NON_MAGIC,
                            false, cellLines));
                    cellLines = null;
                    cellMagic = false;
                }
                snippets.add(new MagicSnippet(MagicSnippet.Type.MAGIC_LINE,
                        true, List.of(new MagicSnippet.CodeLine(line, hasPosition, relativePosition, position))));
                continue;
            }

            // found a cell magic start
            if (hasCellMarker(line)) {
                if (cellLines != null) {
                    // collect accumulation
                    snippets.add(new MagicSnippet(
                            cellMagic ? MagicSnippet.Type.MAGIC_CELL : MagicSnippet.Type.NON_MAGIC,
                            false, cellLines));
                }
                cellLines = new ArrayList<>();
                cellMagic = true;
                cellLines.add(new MagicSnippet.CodeLine(line, hasPosition, relativePosition, position));
                continue;
            }

            // line after other lines
            if (cellLines != null) {
                cellLines.add(new MagicSnippet.CodeLine(line, hasPosition, relativePosition, position));
                continue;
            }

            // non-magic line since there are no previous lines
            cellLines = new ArrayList<>();
            cellLines.add(new MagicSnippet.CodeLine(line, hasPosition, relativePosition, position));
        }
        // collect what remains
        if (cellLines != null) {
            snippets.add(new MagicSnippet(
                    cellMagic ? MagicSnippet.Type.MAGIC_CELL : MagicSnippet.Type.NON_MAGIC,
                    false, cellLines));
        }
        return snippets;
    }

    private boolean hasLineMarker(String line) {
        if (line.length() < 2) {
            return false;
        }
        return line.charAt(0) == '%' && line.charAt(1) != '%' && !line.substring(1).trim().isEmpty();
    }

    private boolean hasCellMarker(String line) {
        if (line.length() < 2) {
            return false;
        }
        return line.charAt(0) == '%' && line.charAt(1) == '%' && !line.substring(2).trim().isEmpty();
    }

    private boolean hasComment(String line) {
        return line.startsWith("//");
    }

    private boolean isEmpty(String line) {
        return line.trim().isEmpty();
    }

    public boolean canHandleByMagic(List<MagicSnippet> magicSnippets) {
        for (var magicSnippet : magicSnippets) {
            if (magicSnippet.type() == MagicSnippet.Type.NON_MAGIC) {
                return false;
            }
        }
        return true;
    }

    public boolean containsMixedSnippets(List<MagicSnippet> magicSnippets) {
        boolean hasMagic = false;
        boolean hasNonMagic = false;
        for (var magicSnippet : magicSnippets) {
            switch (magicSnippet.type()) {
                case MAGIC_LINE, MAGIC_CELL -> hasMagic = true;
                case NON_MAGIC -> hasNonMagic = true;
                default -> {
                }
            }
        }
        return hasMagic && hasNonMagic;
    }
}
