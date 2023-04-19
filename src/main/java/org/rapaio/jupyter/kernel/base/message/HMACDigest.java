package org.rapaio.jupyter.kernel.base.message;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMACDigest {

    public static final HMACDigest NO_AUTH_INSTANCE = new HMACDigest();

    private final static char[] HEX_CHAR = "0123456789abcdef".toCharArray();
    private final static int LOW_DIGIT = 0x0F;
    private final Mac mac;

    private HMACDigest() {
        this.mac = null;
    }

    public HMACDigest(String algorithm, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        if (algorithm == null || key == null) {
            throw new IllegalArgumentException();
        } else {
            this.mac = Mac.getInstance(algorithm.replace("-", ""));
            this.mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.US_ASCII), algorithm));
        }
    }

    public synchronized String calculateSignature(byte[]... messageParts) {
        if (mac == null) {
            return "";
        }

        for (byte[] part : messageParts) {
            this.mac.update(part);
        }
        byte[] sig = this.mac.doFinal();

        char[] hex = new char[sig.length * 2];
        for (int j = 0; j < sig.length; j++) {
            hex[j * 2] = HEX_CHAR[(sig[j] >>> 4) & LOW_DIGIT];
            hex[j * 2 + 1] = HEX_CHAR[sig[j] & LOW_DIGIT];
        }

        return String.valueOf(hex);
    }
}
