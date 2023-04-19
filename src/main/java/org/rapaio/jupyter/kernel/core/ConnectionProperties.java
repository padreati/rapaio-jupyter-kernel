package org.rapaio.jupyter.kernel.core;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.rapaio.jupyter.kernel.message.HMACDigest;

import com.google.gson.annotations.SerializedName;

public record ConnectionProperties(
        @SerializedName("control_port") int controlPort,
        @SerializedName("shell_port") int shellPort,
        @SerializedName("transport") String transport,
        @SerializedName("signature_scheme") String signatureScheme,
        @SerializedName("stdin_port") int stdinPort,
        @SerializedName("hb_port") int hbPort,
        @SerializedName("ip") String ip,
        @SerializedName("iopub_port") int iopubPort,
        @SerializedName("key") String key) {

    public HMACDigest createHMACDigest() throws InvalidKeyException, NoSuchAlgorithmException {
        if (key == null || key.isEmpty()) {
            return HMACDigest.NO_AUTH_INSTANCE;
        }
        return new HMACDigest(signatureScheme, key);
    }
}
