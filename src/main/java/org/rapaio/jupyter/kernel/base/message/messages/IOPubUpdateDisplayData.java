package org.rapaio.jupyter.kernel.base.message.messages;

import org.rapaio.jupyter.base.core.display.DisplayData;
import org.rapaio.jupyter.kernel.base.message.ContentType;
import org.rapaio.jupyter.kernel.base.message.MessageType;

public class IOPubUpdateDisplayData extends DisplayData implements ContentType<IOPubUpdateDisplayData> {

    @Override
    public MessageType<IOPubUpdateDisplayData> type() {
        return MessageType.IOPUB_UPDATE_DISPLAY_DATA;
    }

    public IOPubUpdateDisplayData(DisplayData data) {
        super(data);

        if (!data.hasDisplayId()) {
            throw new IllegalArgumentException("In order to update a display, the data must have a display_id.");
        }
    }
}
