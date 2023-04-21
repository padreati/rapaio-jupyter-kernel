package org.rapaio.jupyter.kernel.message.adapters;

import java.lang.reflect.Type;

import org.rapaio.jupyter.kernel.core.history.HistoryEntry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


public class HistoryEntryAdapter implements JsonSerializer<HistoryEntry> {
    public static final HistoryEntryAdapter INSTANCE = new HistoryEntryAdapter();

    private HistoryEntryAdapter() {
    }

    @Override
    public JsonElement serialize(HistoryEntry src, Type type, JsonSerializationContext ctx) {
        JsonArray tuple = new JsonArray();

        tuple.add(src.session());
        tuple.add(src.cellNumber());

        if (src.hasOutput()) {
            JsonArray array = new JsonArray();
            array.add(src.input());
            array.add(src.output());
            tuple.add(array);
        } else {
            tuple.add(src.input());
        }

        return tuple;
    }
}
