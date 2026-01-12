package org.rapaio.jupyter.kernel;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public final class GeneralProperties {

    public static GeneralProperties defaultProperties() {
        return new GeneralProperties("default");
    }

    private final Properties properties;

    public GeneralProperties(String profile) {
        try {
            properties = new Properties();
            properties.load(GeneralProperties.class.getClassLoader().getResourceAsStream("profile-" + profile + ".properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getKernelName() {
        return properties.getProperty("kernel.name");
    }

    public String getKernelVersion() {
        return properties.getProperty("kernel.version");
    }

    public String getDefaultDisplayName() {
        return properties.getProperty("default.display.name");
    }

    public String getDefaultKernelDir() {
        return properties.getProperty("default.kernel.dir");
    }

    public String getDefaultJarName() {
        return properties.getProperty("default.jar.name");
    }

    public String getDefaultTimeoutMillis() {
        return properties.getProperty("default.timeout.limits");
    }

    public String getDefaultCompilerOptions() {
        return properties.getProperty("default.compiler.options");
    }

    public String getDefaultInitScript() {
        return properties.getProperty("default.init.script");
    }

    public String getDefaultMimaCache() {
        return properties.getProperty("default.mima.cache");
    }

    public String[] getDefaultJavaArgv() {
        String javaArgv = properties.getProperty("default.java.argv");
        if (javaArgv == null) {
            return new String[0];
        }
        return Arrays.stream(javaArgv.split(" ")).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }
}
