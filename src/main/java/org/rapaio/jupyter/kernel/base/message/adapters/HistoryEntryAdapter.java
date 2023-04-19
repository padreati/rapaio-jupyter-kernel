package org.rapaio.jupyter.kernel.base.message.adapters;

import java.lang.reflect.Type;

import org.rapaio.jupyter.base.core.history.HistoryEntry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


public class HistoryEntryAdapter implements JsonSerializer<HistoryEntry> {
    public static final HistoryEntryAdapter INSTANCE = new HistoryEntryAdapter();

    private HistoryEntryAdapter() { }

    @Override
    public JsonElement serialize(HistoryEntry src, Type type, JsonSerializationContext ctx) {
        JsonArray tuple = new JsonArray();

        tuple.add(src.getSession());
        tuple.add(src.getCellNumber());

        if (src.hasOutput()) {
            JsonArray ioPair = new JsonArray();

            ioPair.add(src.getInput());
            ioPair.add(src.getOutput());

            tuple.add(ioPair);
        } else {
            tuple.add(src.getInput());
        }

        return tuple;
    }
}
