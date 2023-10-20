package org.rapaio.jupyter.kernel.message.adapters;

import java.lang.reflect.Type;

import org.rapaio.jupyter.kernel.message.messages.ShellHistoryRequest;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

public class HistoryRequestAdapter implements JsonDeserializer<ShellHistoryRequest> {
    public static final HistoryRequestAdapter INSTANCE = new HistoryRequestAdapter();

    private HistoryRequestAdapter() { }

    @Override
    public ShellHistoryRequest deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();
        JsonPrimitive accessTypeRaw = object.getAsJsonPrimitive("hist_access_type");

        ShellHistoryRequest.AccessType accessType = ctx.deserialize(accessTypeRaw, ShellHistoryRequest.AccessType.class);
        return switch (accessType) {
            case RANGE -> ctx.deserialize(element, ShellHistoryRequest.Range.class);
            case TAIL -> ctx.deserialize(element, ShellHistoryRequest.Tail.class);
            case SEARCH -> ctx.deserialize(element, ShellHistoryRequest.Search.class);
        };
    }
}
