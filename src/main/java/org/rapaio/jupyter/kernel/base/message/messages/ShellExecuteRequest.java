package org.rapaio.jupyter.kernel.base.message.messages;

import java.util.Map;

import org.rapaio.jupyter.kernel.base.message.ContentType;
import org.rapaio.jupyter.kernel.base.message.MessageType;

import com.google.gson.annotations.SerializedName;

public record ShellExecuteRequest(
        @SerializedName("code") String code,
        @SerializedName("silent") boolean silent,
        @SerializedName("store_history") boolean storeHistory,
        /*
         * A bank of {@code name -> code} that need to be evaluated.
         *
         * The idea behind it is that a front end may always want {@code path -> `pwd`}
         * so that they can display where the kernel is.
         */
        @SerializedName("user_expressions") Map<String, String> userExpr,
        @SerializedName("allow_stdin") boolean stdinEnabled,
        @SerializedName("stop_on_error") boolean stopOnError
) implements ContentType<ShellExecuteRequest> {

    @Override
    public MessageType<ShellExecuteRequest> type() {
        return MessageType.SHELL_EXECUTE_REQUEST;
    }

    @Override
    public String toString() {
        return "ExecuteRequest{" +
                "code='" + code + '\'' +
                ", silent=" + silent +
                ", storeHistory=" + storeHistory +
                ", userExpr=" + userExpr +
                ", stdinEnabled=" + stdinEnabled +
                ", stopOnError=" + stopOnError +
                '}';
    }
}
