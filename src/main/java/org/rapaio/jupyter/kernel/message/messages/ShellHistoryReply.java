package org.rapaio.jupyter.kernel.message.messages;

import java.util.List;

import org.rapaio.jupyter.kernel.core.history.HistoryEntry;
import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

public record ShellHistoryReply(List<HistoryEntry> history) implements ContentType<ShellHistoryReply> {

    @Override
    public MessageType<ShellHistoryReply> type() {
        return MessageType.SHELL_HISTORY_REPLY;
    }
}
