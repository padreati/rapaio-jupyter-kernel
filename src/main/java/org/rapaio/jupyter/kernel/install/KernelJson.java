package org.rapaio.jupyter.kernel.install;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public record KernelJson(
        @SerializedName("argv") String[] argv,
        @SerializedName("display_name") String displayName,
        @SerializedName("language") String language,
        @SerializedName("interrupt_mode") String interruptMode,
        @SerializedName("map") Map<String, String> env) {
}
