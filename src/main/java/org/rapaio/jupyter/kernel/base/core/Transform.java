package org.rapaio.jupyter.kernel.base.core;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import org.rapaio.jupyter.base.core.ExpressionValue;
import org.rapaio.jupyter.base.core.history.HistoryEntry;
import org.rapaio.jupyter.kernel.base.message.ContentType;
import org.rapaio.jupyter.kernel.base.message.Header;
import org.rapaio.jupyter.kernel.base.message.KernelTimestamp;
import org.rapaio.jupyter.kernel.base.message.MessageType;
import org.rapaio.jupyter.kernel.base.message.adapters.ExpressionValueAdapter;
import org.rapaio.jupyter.kernel.base.message.adapters.HeaderAdapter;
import org.rapaio.jupyter.kernel.base.message.adapters.HistoryEntryAdapter;
import org.rapaio.jupyter.kernel.base.message.adapters.HistoryRequestAdapter;
import org.rapaio.jupyter.kernel.base.message.adapters.KernelTimestampAdapter;
import org.rapaio.jupyter.kernel.base.message.adapters.MessageTypeAdapter;
import org.rapaio.jupyter.kernel.base.message.adapters.PublishStatusAdapter;
import org.rapaio.jupyter.kernel.base.message.adapters.ReplyTypeAdapter;
import org.rapaio.jupyter.kernel.base.message.messages.IOPubStatus;
import org.rapaio.jupyter.kernel.base.message.messages.ShellHistoryRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class Transform {

    public static final Charset ASCII = StandardCharsets.US_ASCII;
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    public static final byte[] IDENTITY_DELIMITER = "<IDS|MSG>".getBytes(ASCII);

    private static final byte[] EMPTY_JSON = "{}".getBytes(UTF_8);
    private static final Type JSON_OBJ_AS_MAP = new TypeToken<Map<String, Object>>() {
    }.getType();


    private static final Gson replyGson = new GsonBuilder()
            .registerTypeAdapter(HistoryEntry.class, HistoryEntryAdapter.INSTANCE)
            .registerTypeAdapter(ExpressionValue.class, ExpressionValueAdapter.INSTANCE)
            .create();

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(KernelTimestamp.class, KernelTimestampAdapter.INSTANCE)
            .registerTypeAdapter(Header.class, HeaderAdapter.INSTANCE)
            .registerTypeAdapter(MessageType.class, MessageTypeAdapter.INSTANCE)
            .registerTypeAdapter(IOPubStatus.class, PublishStatusAdapter.INSTANCE)
            .registerTypeAdapter(ShellHistoryRequest.class, HistoryRequestAdapter.INSTANCE)
            .registerTypeHierarchyAdapter(ContentType.class, new ReplyTypeAdapter(replyGson))
            .create();

    public static boolean equalsIdentityDelimiter(byte[] raw) {
        return Arrays.equals(IDENTITY_DELIMITER, raw);
    }

    public static <T> T fromJson(String text, Class<T> clazz) {
        return gson.fromJson(text, clazz);
    }

    public static <T> T fromJson(byte[] raw, Class<T> clazz) {
        return gson.fromJson(new String(raw, UTF_8), clazz);
    }

    public static <T> T fromJsonNull(byte[] raw, Class<T> clazz) {
        JsonElement parentHeaderJson = JsonParser.parseString(new String(raw, UTF_8));
        if (parentHeaderJson.isJsonObject() && parentHeaderJson.getAsJsonObject().size() > 0) {
            return gson.fromJson(parentHeaderJson, clazz);
        }
        return null;
    }

    public static Map<String, Object> fromJsonMap(byte[] raw) {
        return gson.fromJson(new String(raw, UTF_8), JSON_OBJ_AS_MAP);
    }

    public static String toJson(Object object) {
        if (object == null) {
            return "{}";
        }
        return gson.toJson(object);
    }

    public static byte[] toJsonBytes(Object object) {
        if (object == null) {
            return EMPTY_JSON;
        }
        return gson.toJson(object).getBytes(UTF_8);
    }
}
