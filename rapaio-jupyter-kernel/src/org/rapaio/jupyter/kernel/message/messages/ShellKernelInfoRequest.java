package org.rapaio.jupyter.kernel.message.messages;

import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

public record ShellKernelInfoRequest() implements ContentType<ShellKernelInfoRequest> {

    @Override
    public MessageType<ShellKernelInfoRequest> type() {
        return MessageType.SHELL_KERNEL_INFO_REQUEST;
    }
}
