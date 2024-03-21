package org.rapaio.jupyter.kernel.install;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.rapaio.jupyter.kernel.GeneralProperties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Installer {

    private static final String INSTALL_FLAG_AUTO = "-auto";
    private static final String INSTALL_FLAG_SYSTEM = "-system";

    private enum OSName {
        LINUX,
        WINDOWS,
        MAC,
        SOLARIS
    }

    private OSName findOSName() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return OSName.WINDOWS;
        } else if (os.contains("mac")) {
            return OSName.MAC;
        } else if (os.contains("sunos")) {
            return OSName.SOLARIS;
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return OSName.LINUX;
        }
        return null;
    }

    private String getUserHome() {
        String home = System.getProperty("user.home");
        if (home != null && home.endsWith("/")) {
            home = home.substring(0, home.length() - 1);
        }
        return home;
    }

    /**
     * Obtain a list of installation paths depending on the operating system.
     * <p>
     * The list of paths is produced according to the documentation found here:
     * <a href="https://jupyter-client.readthedocs.io/en/stable/kernels.html#kernel-specs">kernel-spec</a>
     *
     * @param os operating system
     * @return list of installation paths ordered by priority, from most important in descending order
     */
    private List<String> getInstallationPaths(OSName os) {

        return switch (os) {
            case LINUX, SOLARIS -> List.of(
                    Path.of(getUserHome(), ".local", "share", "jupyter", "kernels").toString(),
                    Path.of("/usr", "local", "share", "jupyter", "kernels").toString(),
                    Path.of("/usr", "share", "jupyter", "kernels").toString()
            );
            case MAC -> List.of(
                    Path.of(getUserHome(), "Library", "Jupyter", "kernels").toString(),
                    Path.of("/usr", "local", "share", "jupyter", "kernels").toString(),
                    Path.of("/usr", "share", "jupyter", "kernels").toString()
            );
            case WINDOWS -> List.of(
                    Path.of(System.getenv("APPDATA"), "jupyter", "kernels").toString(),
                    Path.of(System.getenv("PROGRAMDATA"), "jupyter", "kernels").toString()
            );
        };
    }

    private boolean hasFlag(String[] args, String flagArg) {
        if (args == null || args.length == 0) {
            return false;
        }
        return Arrays.asList(args).contains(flagArg);
    }

    public void install(String[] args) throws IOException, URISyntaxException {

        boolean autoInstall = hasFlag(args, INSTALL_FLAG_AUTO);
        if (autoInstall) {
            System.out.println("Installing in automatic mode.");
        } else {
            System.out.println("Installing in interactive mode.");
        }

        boolean system = hasFlag(args, INSTALL_FLAG_SYSTEM);

        OSName os = findOSName();
        if (os == null) {
            throw new RuntimeException("Operating system is not recognized. Installation failed.");
        }

        String installationPath = collectInstallationPath(os, autoInstall, system);
        String kernelDir = collectKernelDir(autoInstall);
        String displayName = collectDisplayName(autoInstall);
        String timeoutMillis = collectTimeoutMillis(autoInstall);
        String compilerOptions = collectCompilerOptions(autoInstall);
        String initScript = collectInitScript(autoInstall);

        KernelJson kj = new KernelJsonBuilder()
                .withJarPath(installationPath)
                .withKernelDir(kernelDir)
                .withDisplayName(displayName)
                .withEnvTimeoutMillis(timeoutMillis)
                .withEnvCompilerOptions(compilerOptions)
                .withEnvInitScript(initScript)
                .build();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(kj);


        System.out.println("Installation path: " + installationPath);
        System.out.println("Kernel dir: " + kernelDir);
        System.out.println("kernel.json: ");
        System.out.println(json);

        if (!autoInstall) {
            System.out.println("Installer is ready. Do you want to continue [Y|y/N|n] ?");
            String line = new Scanner(System.in).nextLine().trim();
            if (!line.toLowerCase(Locale.ROOT).equals("y")) {
                System.out.println("Installation process was canceled. "
                        + "Please note that you need to answer with capital Y if you want the installer to continue.");
                System.exit(0);
            }
        }

        // create installation directory if it does not exist
        File installPathFile = new File(installationPath);
        if (!installPathFile.exists()) {
            if (!installPathFile.mkdirs()) {
                System.out.println("Could not create installation directory. Check if it is a valid path or there are enough permissions.");
                System.exit(1);
            }
        }

        // delete kernel dir if exists
        File kernelDirFile = new File(installPathFile, kernelDir);
        if (kernelDirFile.exists()) {
            deleteRecursive(kernelDirFile);
        }

        // create kernel dir
        if (!kernelDirFile.mkdir()) {
            System.out.println("Could not create kernel directory: " + kernelDirFile.getAbsolutePath());
            System.exit(1);
        }

        createKernelJson(kernelDirFile, json);
        copyJarFile(kernelDirFile);
    }

    private void createKernelJson(File kernelDir, String serializedJson) throws IOException {
        try (FileWriter w = new FileWriter(new File(kernelDir, "kernel.json"))) {
            w.append(serializedJson);
            w.flush();
        }
    }

    void copyJarFile(File kernelDir) throws IOException, URISyntaxException {
        Path jarPath = Path.of(Installer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path jarName = jarPath.getFileName();
        Files.copy(jarPath, Path.of(kernelDir.getAbsolutePath(), jarName.toString()), StandardCopyOption.COPY_ATTRIBUTES);
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            var children = file.listFiles();
            if (children != null) {
                for (var child : children) {
                    deleteRecursive(child);
                }
            }
        }
        if (!file.delete()) {
            System.out.println("Could not delete: " + file.getAbsolutePath());
            System.exit(-1);
        }
    }

    private String collectInstallationPath(OSName os, boolean autoInstall, boolean system) {
        List<String> paths = getInstallationPaths(os);
        if (autoInstall) {
            return !system ? paths.getFirst() : paths.getLast();
        }
        return selectInteractivePath(paths);
    }

    private String collectKernelDir(boolean autoInstall) {
        if (autoInstall) {
            return GeneralProperties.getDefaultKernelDir();
        }
        System.out.printf("Select kernel dir (default '%s'):%n", GeneralProperties.getDefaultKernelDir());
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                return GeneralProperties.getDefaultKernelDir();
            }
            if (!line.matches("[a-zA-Z0-9_.-]*")) {
                System.out.println("Invalid name, must contain only alphanumeric, hyphen, underscore or dots.");
                continue;
            }
            return line;
        }
    }

    private String collectDisplayName(boolean autoInstall) {
        if (autoInstall) {
            return GeneralProperties.getDefaultDisplayName();
        }
        System.out.printf("Select display name (default '%s'):%n", GeneralProperties.getDefaultDisplayName());
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            return GeneralProperties.getDefaultDisplayName();
        }
        return line;
    }

    private String collectTimeoutMillis(boolean autoInstall) {
        if (autoInstall) {
            return GeneralProperties.getDefaultTimeoutMillis();
        }
        System.out.println("Select timeout in milliseconds:");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                return GeneralProperties.getDefaultTimeoutMillis();
            }
            try {
                Long.parseLong(line);
                return line;
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection. Must be a long number.");
            }
        }
    }

    private String collectCompilerOptions(boolean autoInstall) {
        if (autoInstall) {
            return GeneralProperties.getDefaultCompilerOptions();
        }
        System.out.printf("Select compiler options (default '%s'):%n", GeneralProperties.getDefaultCompilerOptions());
        return new Scanner(System.in).nextLine().trim();
    }

    private String collectInitScript(boolean autoInstall) {
        if (autoInstall) {
            return GeneralProperties.getDefaultInitScript();
        }
        System.out.printf("Select init script (default '%s':%n", GeneralProperties.getDefaultInitScript());
        return new Scanner(System.in).nextLine().trim();
    }

    private String selectInteractivePath(List<String> options) {
        System.out.println("Select installation path" + ":");
        for (int i = 0; i < options.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + options.get(i));
        }
        Scanner scanner = new Scanner(System.in);
        String errorMessage = "Invalid selection. Must be an integer number from 1 to " + options.size();
        while (true) {
            String line = scanner.nextLine();
            int selection;
            try {
                selection = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println(errorMessage);
                continue;
            }
            if (selection > 0 && selection <= options.size()) {
                return options.get(selection - 1);
            }
            System.out.println(errorMessage);
        }
    }
}
