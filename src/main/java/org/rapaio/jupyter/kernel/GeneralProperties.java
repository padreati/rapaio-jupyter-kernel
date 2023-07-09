package org.rapaio.jupyter.kernel;

import java.io.IOException;
import java.util.Properties;

public final class GeneralProperties {

    private static final String KEY_KERNEL_NAME = "kernel.name";
    private static final String KEY_KERNEL_VERSION = "kernel.version";
    private static final GeneralProperties instance = new GeneralProperties();

    public static GeneralProperties getInstance() {
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

    public String getKernelName() {
        return properties.getProperty(KEY_KERNEL_NAME);
    }

    public String getKernelVersion() {
        return properties.getProperty(KEY_KERNEL_VERSION);
    }
}
