package org.rapaio.jupyter.kernel.message.messages;

import java.util.Map;

import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

import com.google.gson.annotations.SerializedName;

public record ShellCommInfoReply(
        // A map of uuid to target_name for the comms
        Map<String, CommInfo> comms) implements ContentType<ShellCommInfoReply> {

    @Override
    public MessageType<ShellCommInfoReply> type() {
        return MessageType.SHELL_COMM_INFO_REPLY;
    }

    public record CommInfo(@SerializedName("target_name") String targetName) {
    }
}
