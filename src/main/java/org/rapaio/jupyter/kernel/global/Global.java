package org.rapaio.jupyter.kernel.global;

import java.util.UUID;

import org.rapaio.jupyter.kernel.MainApp;
import org.rapaio.jupyter.kernel.core.display.DisplayData;

public final class Global {

    public static String display(Object o, String... args) {
        DisplayData data = MainApp.kernel.getRenderer().render(o, args);

        String id = data.getDisplayId();
        if (id == null) {
            id = UUID.randomUUID().toString();
            data.setDisplayId(id);
        }
        MainApp.kernel.display(data);
        return id;
    }

    public static void updateDisplay(String id, Object o, String... args) {
        DisplayData data = MainApp.kernel.getRenderer().render(o, args);
        MainApp.kernel.updateDisplay(id, data);
    }
}
