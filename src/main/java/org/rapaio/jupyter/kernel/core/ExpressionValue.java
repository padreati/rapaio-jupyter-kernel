package org.rapaio.jupyter.kernel.core;

import java.util.List;

import org.rapaio.jupyter.kernel.core.display.DisplayData;

import com.google.gson.annotations.SerializedName;

public interface ExpressionValue {

    boolean isSuccess();

    record Success(DisplayData data) implements ExpressionValue {

        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    record Error(
            @SerializedName("ename") String errName,
            @SerializedName("evalue") String errMsg,
            @SerializedName("traceback") List<String> stacktrace) implements ExpressionValue {

        @Override
        public boolean isSuccess() {
            return false;
        }
    }
}
