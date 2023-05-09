package org.rapaio.jupyter.kernel.message.adapters;

import java.lang.reflect.Type;

import org.rapaio.jupyter.kernel.message.Header;
import org.rapaio.jupyter.kernel.message.MessageType;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class HeaderAdapter implements JsonSerializer<Header<?>>, JsonDeserializer<Header<?>> {
    public static final HeaderAdapter INSTANCE = new HeaderAdapter();

    private HeaderAdapter() {
    }

    @Override
    public Header<?> deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();
        return new Header<>(
                object.get("msg_id").getAsString(),
                object.get("username").getAsString(),
                object.get("session").getAsString(),
                object.get("date").getAsString(),
                ctx.deserialize(object.get("msg_type"), MessageType.class),
                object.get("version").getAsString()
        );
    }

    @Override
    public JsonElement serialize(Header header, Type type, JsonSerializationContext ctx) {
        JsonObject object = new JsonObject();

        object.addProperty("msg_id", header.id());
        object.addProperty("username", header.username());
        object.addProperty("session", header.sessionId());
        object.addProperty("date", header.timestamp());
        object.add("msg_type", ctx.serialize(header.type()));
        object.addProperty("version", header.version());

        return object;
    }
}
