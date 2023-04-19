package org.rapaio.jupyter.kernel.base.message.messages;

import org.rapaio.jupyter.base.core.display.DisplayData;
import org.rapaio.jupyter.kernel.base.message.ContentType;
import org.rapaio.jupyter.kernel.base.message.MessageType;

public class IOPubDisplayData extends DisplayData implements ContentType<IOPubDisplayData> {

    @Override
    public MessageType<IOPubDisplayData> type() {
        return MessageType.IOPUB_DISPLAY_DATA;
    }

    public IOPubDisplayData(DisplayData data) {
        super(data);
    }
}
