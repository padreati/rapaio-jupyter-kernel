package org.rapaio.jupyter.kernel.base.message.messages;

import org.rapaio.jupyter.base.core.display.DisplayData;
import org.rapaio.jupyter.kernel.base.message.ContentType;
import org.rapaio.jupyter.kernel.base.message.MessageType;

public class ShellInspectReply extends DisplayData implements ContentType<ShellInspectReply> {

    @Override
    public MessageType<ShellInspectReply> type() {
        return MessageType.SHELL_INSPECT_REPLY;
    }

    protected final String status = "ok";
    protected final boolean found;

    public ShellInspectReply(boolean found, DisplayData data) {
        super(data);
        this.found = found;
    }

    public String status() {
        return status;
    }

    public boolean found() {
        return found;
    }
}
