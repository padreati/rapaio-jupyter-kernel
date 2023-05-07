package org.rapaio.jupyter.kernel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.rapaio.jupyter.kernel.channels.JupyterChannels;
import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.Transform;
import org.rapaio.jupyter.kernel.install.Installer;

public class MainApp {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getSimpleName());
    public static final RapaioKernel kernel = new RapaioKernel();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Kernel should have at least one argument. You can start it either with -i option for "
                    + "launching installer, either with connection file name as argument to work as a kernel.");
        }

        if (args[0].equals("-i")) {
            // run installer
            String[] installerArgs = Arrays.copyOfRange(args, 1, args.length);
            new Installer().install(installerArgs);
        } else {
            // run kernel called by jupyter
            runKernel(args[0]);
        }
    }

    private static void runKernel(String connectionFileArg) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        LogManager.getLogManager().readConfiguration(MainApp.class.getClassLoader().getResourceAsStream("logging.properties"));
        Path connectionFile = Paths.get(connectionFileArg);

        if (!Files.isRegularFile(connectionFile)) {
            throw new IllegalArgumentException("Connection file '" + connectionFile + "' isn't a file.");
        }

        String contents = new String(Files.readAllBytes(connectionFile));

        ConnectionProperties connProps = Transform.fromJson(contents, ConnectionProperties.class);
        LOGGER.info("Kernel connection file content: " + contents);

        JupyterChannels connection = new JupyterChannels(connProps, kernel);
        connection.connect();
        connection.joinUntilClose();
    }
}
