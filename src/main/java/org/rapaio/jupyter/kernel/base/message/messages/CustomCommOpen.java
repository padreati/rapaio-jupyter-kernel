package org.rapaio.jupyter.kernel.base.message.messages;

import org.rapaio.jupyter.kernel.base.message.ContentType;
import org.rapaio.jupyter.kernel.base.message.adapters.IdentityJsonElementAdapter;
import org.rapaio.jupyter.kernel.base.message.MessageType;

import com.google.gson.JsonObject;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

public record CustomCommOpen(
        @SerializedName("comm_id") String commId,
        @SerializedName("target_name") String targetName,
        @JsonAdapter(IdentityJsonElementAdapter.class) JsonObject data) implements ContentType<CustomCommOpen> {

    @Override
    public MessageType<CustomCommOpen> type() {
        return MessageType.CUSTOM_COMM_OPEN;
    }
}
