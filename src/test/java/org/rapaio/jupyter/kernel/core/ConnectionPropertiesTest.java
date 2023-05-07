package org.rapaio.jupyter.kernel.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.message.HMACDigest;

public class ConnectionPropertiesTest {

    @Test
    void testSerialization() throws NoSuchAlgorithmException, InvalidKeyException {

        String text = """
                {
                  "control_port": 50160,
                  "shell_port": 57503,
                  "transport": "tcp",
                  "signature_scheme": "hmac-sha256",
                  "stdin_port": 52597,
                  "hb_port": 42540,
                  "ip": "127.0.0.1",
                  "iopub_port": 40885,
                  "key": "a0436f6c-1916-498b-8eb9-e81ab9368e84"
                }
                                """;

        var kcp = Transform.fromJson(text, ConnectionProperties.class);
        assertEquals(50160, kcp.controlPort());
        assertEquals(57503, kcp.shellPort());
        assertEquals("tcp", kcp.transport());

        var hmacGen = kcp.createHMACDigest();

        byte[][] buffers = new byte[][] {
                "test1".getBytes(StandardCharsets.US_ASCII),
                "test2".getBytes(StandardCharsets.US_ASCII)
        };

        String hmac = hmacGen.computeSignature(buffers);
        assertEquals(hmac, hmacGen.computeSignature(buffers));
        assertEquals("", HMACDigest.NO_AUTH_INSTANCE.computeSignature(buffers));
    }
}
