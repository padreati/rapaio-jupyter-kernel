package org.rapaio.jupyter.kernel.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.rapaio.jupyter.kernel.TestUtils;
import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.java.CompilerException;
import org.rapaio.jupyter.kernel.message.Message;
import org.rapaio.jupyter.kernel.message.MessageType;
import org.rapaio.jupyter.kernel.message.messages.ShellInspectReply;
import org.rapaio.jupyter.kernel.message.messages.ShellInspectRequest;

public class RapaioKernelTest {

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
    void compileErrorTest() {
        String s = """
                // test
                %%jshell option
                line 1 of command
                line 2 of command
                """;
        CompilerException ce = assertThrows(CompilerException.class, () -> kernel.eval(s));
        assertNotNull(ce);
    }

    @Test
    void inspectRequestTest() {
        String code = """
                String id = display("placeholder");
                for(int i=0; i<10;i++) {
                    // update output based on the id
                    updateDisplay(id, "update_"+i);
                    Thread.sleep(200);
                }
                """;

        kernel.handleInspectRequest(new Message<>(MessageType.SHELL_INSPECT_REQUEST, new ShellInspectRequest(code, 18, 0)));

        var captor = ArgumentCaptor.forClass(ShellInspectReply.class);
        Mockito.verify(channels).reply(captor.capture());

        var dd = captor.getValue().data();

        assertNotNull(dd);
        assertEquals(2, dd.size());
        assertTrue(dd.containsKey("text/html"));
        assertTrue(dd.containsKey("text/plain"));
        assertTrue(dd.get("text/plain").toString().contains(".display("));
    }
}
