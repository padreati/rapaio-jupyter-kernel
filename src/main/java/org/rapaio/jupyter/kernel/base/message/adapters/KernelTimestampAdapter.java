package org.rapaio.jupyter.kernel.base.message.adapters;

import java.lang.reflect.Type;

import org.rapaio.jupyter.kernel.base.message.KernelTimestamp;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class KernelTimestampAdapter implements JsonSerializer<KernelTimestamp>, JsonDeserializer<KernelTimestamp> {
    public static final KernelTimestampAdapter INSTANCE = new KernelTimestampAdapter();

    private KernelTimestampAdapter() { }

    @Override
    public KernelTimestamp deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) {
        return new KernelTimestamp(element.getAsString());
    }

    @Override
    public JsonElement serialize(KernelTimestamp timestamp, Type type, JsonSerializationContext ctx) {
        return new JsonPrimitive(timestamp.getDateString());
    }
}
