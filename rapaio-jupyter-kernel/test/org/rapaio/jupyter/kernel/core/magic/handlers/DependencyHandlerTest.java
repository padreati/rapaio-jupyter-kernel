package org.rapaio.jupyter.kernel.core.magic.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.TestUtils;
import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicParseException;

public class DependencyHandlerTest {

    private RapaioKernel kernel;
    private Channels channels;

    @BeforeEach
    void beforeEach() throws NoSuchAlgorithmException, InvalidKeyException {
        kernel = new RapaioKernel();
        channels = TestUtils.spyChannels();
        channels.connect(kernel);
    }

    @AfterEach
    void afterEach() {
        channels.close();
    }

    @Test
    void invalidDependencyTest() throws MagicEvalException, MagicParseException {
        kernel.magicEngine().eval("%dependency /add hgfd:hjgsdfa:1.0.0");
        kernel.magicEngine().eval("%dependency /resolve");

        assertEquals(0, kernel.dependencyManager().getResolvedDependencies().size());
        assertEquals(0, kernel.dependencyManager().getProposedDependencies().size());
        assertEquals(0, kernel.dependencyManager().getLoadedArtifacts().size());

        kernel.magicEngine().eval("%dependency /resolve");

        assertEquals(0, kernel.dependencyManager().getResolvedDependencies().size());
        assertEquals(0, kernel.dependencyManager().getProposedDependencies().size());
        assertEquals(0, kernel.dependencyManager().getLoadedArtifacts().size());

    }

    @Test
    void validDependencyTest() throws MagicEvalException, MagicParseException {
        kernel.magicEngine().eval("%dependency /add io.github.padreati:rapaio-core:6.1.0");
        kernel.magicEngine().eval("%dependency /resolve");

        assertEquals(1, kernel.dependencyManager().getResolvedDependencies().size());
        assertEquals(0, kernel.dependencyManager().getProposedDependencies().size());
        assertEquals(2, kernel.dependencyManager().getLoadedArtifacts().size());

        kernel.magicEngine().eval("%dependency /list-dependencies");
    }
}
