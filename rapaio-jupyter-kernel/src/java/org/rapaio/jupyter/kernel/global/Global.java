package org.rapaio.jupyter.kernel.global;

import java.util.UUID;

import org.rapaio.jupyter.kernel.MainApp;
import org.rapaio.jupyter.kernel.display.DisplayData;
import org.rapaio.jupyter.kernel.display.Display;

public final class Global {

    private static final Config CONFIG = new Config();

    public static Display displaySystem() {
        return Display.inst();
    }

    public static Config config() {
        return CONFIG;
    }

    public static String display(String mime, Object o) {
        DisplayData dd = mime == null || mime.trim().isEmpty() ?
                Display.inst().render(o) :
                Display.inst().render(mime, o);
        String id = UUID.randomUUID().toString();
        dd.setDisplayId(id);
        MainApp.kernel.display(dd);
        return id;
    }

    public static String display(Object o) {
        DisplayData dd = Display.inst().render(o);
        String id = UUID.randomUUID().toString();
        dd.setDisplayId(id);
        MainApp.kernel.display(dd);
        return id;
    }

    public static void updateDisplay(String id, String mimeType, Object o) {
        DisplayData dd = mimeType == null || mimeType.trim().isEmpty() ?
                Display.inst().render(o) :
                Display.inst().render(mimeType, o);
        dd.setDisplayId(id);
        MainApp.kernel.updateDisplay(dd);
    }

    public static void updateDisplay(String id, Object o) {
        DisplayData dd = Display.inst().render(o);
        dd.setDisplayId(id);
        MainApp.kernel.updateDisplay(dd);
    }

    public static Object eval(String code) throws Exception {
        return MainApp.kernel.eval(code);
    }
}
