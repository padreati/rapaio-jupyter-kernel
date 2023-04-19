package org.rapaio.jupyter.kernel.base.message.messages;

import java.util.List;

import org.rapaio.jupyter.base.core.history.HistoryEntry;
import org.rapaio.jupyter.kernel.base.message.ContentType;
import org.rapaio.jupyter.kernel.base.message.MessageType;

public record ShellHistoryReply(List<HistoryEntry> history) implements ContentType<ShellHistoryReply> {

    @Override
    public MessageType<ShellHistoryReply> type() {
        return MessageType.SHELL_HISTORY_REPLY;
    }
}
