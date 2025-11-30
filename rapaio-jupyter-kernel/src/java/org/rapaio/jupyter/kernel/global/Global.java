package org.rapaio.jupyter.kernel.global;

import java.util.UUID;

import org.rapaio.jupyter.kernel.MainApp;
import org.rapaio.jupyter.kernel.display.DisplayData;

public final class Global {

    public static String display(String mimeType, Object o) {
        DisplayData dd = mimeType == null || mimeType.trim().isEmpty() ?
                MainApp.kernel.renderer().render(o) :
                MainApp.kernel.renderer().render(mimeType, o);
        String id = UUID.randomUUID().toString();
        dd.setDisplayId(id);
        MainApp.kernel.display(dd);
        return id;
    }

    public static String display(Object o) {
        DisplayData dd = MainApp.kernel.renderer().render(o);
        String id = UUID.randomUUID().toString();
        dd.setDisplayId(id);
        MainApp.kernel.display(dd);
        return id;
    }

    public static void updateDisplay(String id, String mimeType, Object o) {
        DisplayData dd = mimeType == null || mimeType.trim().isEmpty() ?
                MainApp.kernel.renderer().render(o) :
                MainApp.kernel.renderer().render(mimeType, o);
        dd.setDisplayId(id);
        MainApp.kernel.updateDisplay(dd);
    }

    public static void updateDisplay(String id, Object o) {
        DisplayData dd = MainApp.kernel.renderer().render(o);
        dd.setDisplayId(id);
        MainApp.kernel.updateDisplay(dd);
    }

    public static Object eval(String code) throws Exception {
        return MainApp.kernel.eval(code);
    }
}
