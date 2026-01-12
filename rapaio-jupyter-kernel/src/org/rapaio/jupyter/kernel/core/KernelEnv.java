package org.rapaio.jupyter.kernel.core;

import static org.rapaio.jupyter.kernel.core.RapaioKernel.RJK_CLASSPATH;
import static org.rapaio.jupyter.kernel.core.RapaioKernel.RJK_COMPILER_OPTIONS;
import static org.rapaio.jupyter.kernel.core.RapaioKernel.RJK_INIT_SCRIPT;
import static org.rapaio.jupyter.kernel.core.RapaioKernel.RJK_MIMA_CACHE;
import static org.rapaio.jupyter.kernel.core.RapaioKernel.RJK_TIMEOUT_MILLIS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.rapaio.jupyter.kernel.GeneralProperties;

public class KernelEnv {

    public static final Logger LOGGER = Logger.getLogger(KernelEnv.class.getSimpleName());

    private final List<String> compilerOptions;
    private final long timeoutMillis;
    private final String initScriptContent;
    private final String classpath;
    private final String mimaCache;

    public KernelEnv() {
        String envCompilerOptions = System.getenv(RJK_COMPILER_OPTIONS);
        LOGGER.info(RJK_COMPILER_OPTIONS + " env: " + envCompilerOptions);
        if (envCompilerOptions == null) {
            envCompilerOptions = GeneralProperties.defaultProperties().getDefaultCompilerOptions();
        }
        String[] compilerTokens = Arrays.stream(envCompilerOptions.split("\\s"))
                .filter(s -> !s.trim().isEmpty()).toArray(String[]::new);
        compilerOptions = new ArrayList<>(List.of(compilerTokens));

        String envTimeoutMillis = System.getenv(RJK_TIMEOUT_MILLIS);
        LOGGER.info(RJK_TIMEOUT_MILLIS + " env: " + envTimeoutMillis);
        if (envTimeoutMillis == null) {
            envTimeoutMillis = GeneralProperties.defaultProperties().getDefaultTimeoutMillis();
        }
        try {
            timeoutMillis = Long.parseLong(envTimeoutMillis);
        } catch (NumberFormatException ex) {
            throw new RuntimeException(
                    "Cannot start kernel. Could not parse as long the value specified for env variable " + RJK_TIMEOUT_MILLIS);
        }

        String envInitScript = System.getenv(RJK_INIT_SCRIPT);
        LOGGER.info(RJK_INIT_SCRIPT + " env: " + envInitScript);
        if (envInitScript == null) {
            envInitScript = GeneralProperties.defaultProperties().getDefaultInitScript();
        }
        initScriptContent = envInitScript.trim().isEmpty() ? "" : loadInitScript(envInitScript);

        String envClasspath = System.getenv(RJK_CLASSPATH);
        LOGGER.info(RJK_CLASSPATH + " env: " + envClasspath);
        if (envClasspath == null) {
            envClasspath = "";
        }
        classpath = envClasspath;

        String envMimaCache = System.getenv(RJK_MIMA_CACHE);
        LOGGER.info(RJK_MIMA_CACHE + " env: " + envMimaCache);
        if (envMimaCache == null) {
            envMimaCache = GeneralProperties.defaultProperties().getDefaultMimaCache();
            LOGGER.info("Since " + RJK_MIMA_CACHE + " is empty, default value is '" + envMimaCache + "'");
        }
        mimaCache = envMimaCache;
    }

    private String loadInitScript(String path) {
        LOGGER.info("Loading init script: " + path);
        try {
            File file = new File(path);
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    content.append(line).append("\n");
                }
            }
            return content.toString();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load specified init script: " + path);
        }
    }

    public List<String> compilerOptions() {
        return compilerOptions;
    }

    public long timeoutMillis() {
        return timeoutMillis;
    }

    public String initScriptContent() {
        return initScriptContent;
    }

    public String classpath() {
        return classpath;
    }

    public String mimaCache() {
        return mimaCache;
    }
}
