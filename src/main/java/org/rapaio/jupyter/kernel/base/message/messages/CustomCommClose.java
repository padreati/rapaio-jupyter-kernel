package org.rapaio.jupyter.kernel.base.message.messages;

import org.rapaio.jupyter.kernel.base.message.ContentType;
import org.rapaio.jupyter.kernel.base.message.adapters.IdentityJsonElementAdapter;
import org.rapaio.jupyter.kernel.base.message.MessageType;

import com.google.gson.JsonObject;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

public record CustomCommClose(
        @SerializedName("comm_id") String commId,
        @JsonAdapter(IdentityJsonElementAdapter.class) JsonObject data) implements ContentType<CustomCommClose> {

    @Override
    public MessageType<CustomCommClose> type() {
        return MessageType.CUSTOM_COMM_CLOSE;
    }
}
