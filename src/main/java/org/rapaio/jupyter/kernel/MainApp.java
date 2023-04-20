package org.rapaio.jupyter.kernel;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.rapaio.jupyter.java.RJKMain;
import org.rapaio.jupyter.kernel.channels.JupyterChannels;
import org.rapaio.jupyter.kernel.core.ConnectionProperties;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.Transform;

public class MainApp {

    private static final Logger LOGGER = Logger.getLogger(RJKMain.class.getSimpleName());
    public static final RapaioKernel kernel = new RapaioKernel();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Missing connection file argument");
        }

        Path connectionFile = Paths.get(args[0]);

        if (!Files.isRegularFile(connectionFile)) {
            throw new IllegalArgumentException("Connection file '" + connectionFile + "' isn't a file.");
        }

        String contents = new String(Files.readAllBytes(connectionFile));

        LogManager.getLogManager().readConfiguration(RJKMain.class.getClassLoader().getResourceAsStream("logging.properties"));

        ConnectionProperties connProps = Transform.fromJson(contents, ConnectionProperties.class);
        LOGGER.info("Kernel connected with: " + contents);

        JupyterChannels connection = new JupyterChannels(connProps, kernel);
        connection.connect();
        connection.joinUntilClose();
    }
}
