package org.rapaio.jupyter.kernel.message.adapters;

import java.lang.reflect.Type;

import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.messages.ErrorReply;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class ReplyTypeAdapter implements JsonDeserializer<ContentType<?>> {
    private final Gson replyGson;

    /**
     * <b>Important:</b> the given instance must <b>not</b> have this type
     * adapter registered or deserialization with this deserializer will
     * cause a stack overflow exception.
     *
     * @param replyGson the gson instance to use when deserializing replies.
     */
    public ReplyTypeAdapter(Gson replyGson) {
        this.replyGson = replyGson;
    }

    @Override
    public ContentType<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        // If the reply is an error, decode as an ErrorReply instead of the normal content type
        if (jsonElement.isJsonObject()) {
            JsonElement status = jsonElement.getAsJsonObject().get("status");
            if (status != null && status.isJsonPrimitive()
                    && status.getAsString().equalsIgnoreCase("error"))
                return this.replyGson.fromJson(jsonElement, ErrorReply.class);
        }

        return this.replyGson.fromJson(jsonElement, type);
    }
}
