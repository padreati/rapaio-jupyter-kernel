package org.rapaio.jupyter.kernel.core.java;

import java.util.Map;
import java.util.WeakHashMap;

import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;

public class RapaioExecutionControlProvider implements ExecutionControlProvider {

    public static final String EXECUTION_ID_KEY = "execution_id";
    public static final String EXECUTION_TIMEOUT_MILLIS_KEY = "timeout_millis";

    private final Map<String, RapaioExecutionControl> controllers = new WeakHashMap<>();

    public RapaioExecutionControl getRegisteredControl(String id) {
        return controllers.get(id);
    }

    @Override
    public String name() {
        return "RapaioKernelControlProvider";
    }

    @Override
    public ExecutionControl generate(ExecutionEnv env, Map<String, String> parameters) throws Throwable {
        long timeout = -1;
        if (parameters != null && parameters.containsKey(EXECUTION_TIMEOUT_MILLIS_KEY)) {
            timeout = Long.parseLong(parameters.get(EXECUTION_TIMEOUT_MILLIS_KEY));
        }

        var control = new RapaioExecutionControl(timeout);

        if (parameters != null && parameters.containsKey(EXECUTION_ID_KEY)) {
            String id = parameters.get(EXECUTION_ID_KEY);
            if (id != null) {
                controllers.put(id, control);
            }
        }

        return control;
    }
}
