package org.rapaio.jupyter.kernel.install;

import org.rapaio.jupyter.kernel.core.RapaioKernel;

import java.util.*;

public class KernelJsonBuilder {

    private static final String SEPARATOR = "/";
    private static final String CONNECTION_FILE_MARKER = "{connection_file}";
    private static final String LANGUAGE = "java";
    private static final String INTERRUPT_MODE = "message";

    public static final String DEFAULT_DISPLAY_NAME = "Java RJK";
    public static final String DEFAULT_KERNEL_DIR = "rapaio-jupyter-kernel";
    private static final String DEFAULT_JAR_NAME = "rapaio-jupyter-kernel.jar";

    private String displayName = DEFAULT_DISPLAY_NAME;
    private String kernelDir = DEFAULT_KERNEL_DIR;
    private String jarPath = null;
    private final Map<String, String> env = new HashMap<>() {{
        put(RapaioKernel.RJK_COMPILER_OPTIONS, RapaioKernel.DEFAULT_COMPILER_OPTIONS);
        put(RapaioKernel.RJK_TIMEOUT_MILLIS, RapaioKernel.DEFAULT_RJK_TIMEOUT_MILLIS);
        put(RapaioKernel.RJK_INIT_SCRIPT, RapaioKernel.DEFAULT_INIT_SCRIPT);
    }};

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
            jarName = DEFAULT_JAR_NAME;
        }

        List<String> argv = new ArrayList<>();
        argv.add("java");
        argv.add("--enable-preview");
        argv.add("--add-modules");
        argv.add("jdk.incubator.vector,jdk.incubator.concurrent");
        argv.add("-jar");
        argv.add(jarPath + SEPARATOR + kernelDir + SEPARATOR + jarName);
        argv.add(CONNECTION_FILE_MARKER);

        return new KernelJson(argv.toArray(String[]::new), displayName, LANGUAGE, INTERRUPT_MODE, env);
    }
}
