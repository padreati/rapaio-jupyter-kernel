package org.rapaio.jupyter.kernel;

import java.io.IOException;
import java.util.Properties;

public final class GeneralProperties {

    private static final GeneralProperties instance = new GeneralProperties();

    private static GeneralProperties getInstance() {
        return instance;
    }

    private final Properties properties;

    private GeneralProperties() {
        try {
            properties = new Properties();
            properties.load(GeneralProperties.class.getClassLoader().getResourceAsStream("general.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getKernelName() {
        return getInstance().properties.getProperty("kernel.name");
    }

    public static String getKernelVersion() {
        return getInstance().properties.getProperty("kernel.version");
    }

    public static String getDefaultDisplayName() {
        return getInstance().properties.getProperty("default.display.name");
    }

    public static String getDefaultKernelDir() {
        return getInstance().properties.getProperty("default.kernel.dir");
    }

    public static String getDefaultJarName() {
        return getInstance().properties.getProperty("default.jar.name");
    }

    public static String getDefaultTimeoutMillis() {
        return getInstance().properties.getProperty("default.timeout.limits");
    }

    public static String getDefaultCompilerOptions() {
        return getInstance().properties.getProperty("default.compiler.options");
    }

    public static String getDefaultInitScript() {
        return getInstance().properties.getProperty("default.init.script");
    }
}
