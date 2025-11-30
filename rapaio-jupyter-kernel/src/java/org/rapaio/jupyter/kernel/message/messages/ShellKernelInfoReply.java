package org.rapaio.jupyter.kernel.message.messages;

import java.util.List;

import org.rapaio.jupyter.kernel.core.LanguageInfo;
import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

import com.google.gson.annotations.SerializedName;

public record ShellKernelInfoReply(
        @SerializedName("protocol_version") String protocolVersion,
        @SerializedName("implementation") String implementationName,
        @SerializedName("implementation_version") String implementationVersion,
        @SerializedName("language_info") LanguageInfo langInfo,
        @SerializedName("banner") String banner,
        @SerializedName("help_links") List<LanguageInfo.Help> helpLinks
) implements ContentType<ShellKernelInfoReply> {

    @Override
    public MessageType<ShellKernelInfoReply> type() {
        return MessageType.SHELL_KERNEL_INFO_REPLY;
    }
}
