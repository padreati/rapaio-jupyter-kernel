package org.rapaio.jupyter.kernel.core.history;

public record HistoryEntry(int session, int cellNumber, String input, String output) {

    public HistoryEntry(int session, int cellNumber, String input) {
        this(session, cellNumber, input, null);
    }

    public boolean hasOutput() {
        return output != null;
    }
}
