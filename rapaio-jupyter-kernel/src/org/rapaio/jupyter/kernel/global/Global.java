package org.rapaio.jupyter.kernel.global;

import java.util.UUID;

import org.rapaio.jupyter.kernel.MainApp;
import org.rapaio.jupyter.kernel.display.DisplayData;
import org.rapaio.jupyter.kernel.display.DisplaySystem;

public final class Global {

    private static final Options OPTIONS = new Options();

    public static DisplaySystem displaySystem() {
        return DisplaySystem.inst();
    }

    public static Options options() {
        return OPTIONS;
    }

    public static String display(Object o) {
        return display(null, o);
    }

    public static String display(String mime, Object o) {
        DisplayData dd = mime == null || mime.trim().isEmpty() ?
                DisplaySystem.inst().render(o) :
                DisplaySystem.inst().render(mime, o);
        String id = UUID.randomUUID().toString();
        dd.setDisplayId(id);
        MainApp.kernel.display(dd);
        return id;
    }

    public static void updateDisplay(String id, Object o) {
        updateDisplay(id, null, o);
    }

    public static void updateDisplay(String id, String mimeType, Object o) {
        DisplayData dd = mimeType == null || mimeType.trim().isEmpty() ?
                DisplaySystem.inst().render(o) :
                DisplaySystem.inst().render(mimeType, o);
        dd.setDisplayId(id);
        MainApp.kernel.updateDisplay(dd);
    }

    public static Object eval(String code) throws Exception {
        return MainApp.kernel.eval(code);
    }
}
