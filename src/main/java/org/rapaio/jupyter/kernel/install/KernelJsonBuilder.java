package org.rapaio.jupyter.kernel.install;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.rapaio.jupyter.kernel.GeneralProperties;
import org.rapaio.jupyter.kernel.core.RapaioKernel;

public class KernelJsonBuilder {

    private static final String SEPARATOR = "/";
    private static final String CONNECTION_FILE_MARKER = "{connection_file}";
    private static final String LANGUAGE = "java";
    private static final String INTERRUPT_MODE = "message";

    private String displayName;
    private String kernelDir;
    private String jarPath = null;
    private final Map<String, String> env;
    private final String defaultJarName;
    private final String[] args;

    public KernelJsonBuilder(GeneralProperties properties) {
        displayName = properties.getDefaultDisplayName();
        kernelDir = properties.getDefaultKernelDir();
        env = new HashMap<>() {{
            put(RapaioKernel.RJK_COMPILER_OPTIONS, properties.getDefaultCompilerOptions());
            put(RapaioKernel.RJK_TIMEOUT_MILLIS, properties.getDefaultTimeoutMillis());
            put(RapaioKernel.RJK_INIT_SCRIPT, properties.getDefaultInitScript());
            put(RapaioKernel.RJK_CLASSPATH, "");
        }};
        defaultJarName = properties.getDefaultJarName();
        args = properties.getDefaultJavaArgv();
    }


    private void validate() {
        Objects.requireNonNull(jarPath);
    }

    public KernelJsonBuilder withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public KernelJsonBuilder withKernelDir(String kernelDir) {
        this.kernelDir = kernelDir;
        return this;
    }

    public KernelJsonBuilder withJarPath(String jarPath) {
        if (jarPath.endsWith("/")) {
            jarPath = jarPath.substring(0, jarPath.length() - 1);
        }
        this.jarPath = jarPath;
        return this;
    }

    public KernelJsonBuilder withEnvCompilerOptions(String compilerOptions) {
        env.put(RapaioKernel.RJK_COMPILER_OPTIONS, compilerOptions);
        return this;
    }

    public KernelJsonBuilder withEnvTimeoutMillis(String timeoutMillis) {
        env.put(RapaioKernel.RJK_TIMEOUT_MILLIS, timeoutMillis);
        return this;
    }

    public KernelJsonBuilder withEnvInitScript(String initScript) {
        env.put(RapaioKernel.RJK_INIT_SCRIPT, initScript);
        return this;
    }


    public KernelJson build() {
        validate();

        String fullPath = KernelJsonBuilder.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String jarName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
        if (jarName.isEmpty()) {
            jarName = defaultJarName;
        }

        List<String> argv = new ArrayList<>();
        argv.add("java");
        argv.addAll(Arrays.asList(args));
        argv.add("-jar");
        argv.add(jarPath + SEPARATOR + kernelDir + SEPARATOR + jarName);
        argv.add(CONNECTION_FILE_MARKER);

        return new KernelJson(argv.toArray(String[]::new), displayName, LANGUAGE, INTERRUPT_MODE, env);
    }
}
