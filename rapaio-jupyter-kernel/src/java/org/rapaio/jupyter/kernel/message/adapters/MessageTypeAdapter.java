package org.rapaio.jupyter.kernel.message.adapters;

import java.lang.reflect.Type;

import org.rapaio.jupyter.kernel.message.MessageType;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MessageTypeAdapter implements JsonSerializer<MessageType<?>>, JsonDeserializer<MessageType<?>> {
    public static final MessageTypeAdapter INSTANCE = new MessageTypeAdapter();

    private MessageTypeAdapter() { }

    @Override
    public MessageType<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        return MessageType.getType(jsonElement.getAsString());
    }

    @Override
    public JsonElement serialize(MessageType<?> messageType, Type type, JsonSerializationContext ctx) {
        return new JsonPrimitive(messageType.name());
    }
}
