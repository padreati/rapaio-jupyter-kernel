package org.rapaio.jupyter.kernel.base.message.messages;

import org.rapaio.jupyter.kernel.base.message.ContentType;
import org.rapaio.jupyter.kernel.base.message.MessageType;

public record ShellKernelInfoRequest() implements ContentType<ShellKernelInfoRequest> {

    @Override
    public MessageType<ShellKernelInfoRequest> type() {
        return MessageType.SHELL_KERNEL_INFO_REQUEST;
    }
}
