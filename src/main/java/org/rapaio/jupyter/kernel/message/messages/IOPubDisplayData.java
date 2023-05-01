package org.rapaio.jupyter.kernel.message.messages;

import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

public class IOPubDisplayData extends DisplayData implements ContentType<IOPubDisplayData> {

    @Override
    public MessageType<IOPubDisplayData> type() {
        return MessageType.IOPUB_DISPLAY_DATA;
    }

    public IOPubDisplayData(DisplayData data) {
        super(data.data(), data.metadata(), data.transientData());
    }
}
