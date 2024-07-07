package org.rapaio.jupyter.kernel.global;

import java.util.UUID;

import org.rapaio.jupyter.kernel.MainApp;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.MIMEType;

public final class Global {

    public static String display(String mimeType, Object o) {
        DisplayData displayData = mimeType == null || mimeType.trim().isEmpty() ?
                MainApp.kernel.renderer().render(o) :
                MainApp.kernel.renderer().render(MIMEType.fromString(mimeType), o);
        String id = UUID.randomUUID().toString();
        displayData.setDisplayId(id);
        MainApp.kernel.display(displayData);
        return id;
    }

    public static String display(Object o) {
        DisplayData displayData = MainApp.kernel.renderer().render(o);
        String id = UUID.randomUUID().toString();
        displayData.setDisplayId(id);
        MainApp.kernel.display(displayData);
        return id;
    }

    public static void updateDisplay(String id, String mimeType, Object o) {
        DisplayData displayData = mimeType == null || mimeType.trim().isEmpty() ?
                MainApp.kernel.renderer().render(o) :
                MainApp.kernel.renderer().render(MIMEType.fromString(mimeType), o);
        displayData.setDisplayId(id);
        MainApp.kernel.updateDisplay(displayData);
    }

    public static void updateDisplay(String id, Object o) {
        DisplayData displayData = MainApp.kernel.renderer().render(o);
        displayData.setDisplayId(id);
        MainApp.kernel.updateDisplay(displayData);
    }
}
