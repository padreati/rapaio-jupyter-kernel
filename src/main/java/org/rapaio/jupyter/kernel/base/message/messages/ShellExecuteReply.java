package org.rapaio.jupyter.kernel.base.message.messages;

import java.util.Map;

import org.rapaio.jupyter.base.core.ExpressionValue;
import org.rapaio.jupyter.kernel.base.message.ContentType;
import org.rapaio.jupyter.kernel.base.message.MessageType;

import com.google.gson.annotations.SerializedName;

public record ShellExecuteReply(
        @SerializedName("status") Status status,
        @SerializedName("execution_count") int executionCount,
        @SerializedName("user_expressions") Map<String, ExpressionValue> evaluatedUserExpr
) implements ContentType<ShellExecuteReply> {

    public static ShellExecuteReply withOk(int executionCount, Map<String, ExpressionValue> evaluatedUserExpr) {
        return new ShellExecuteReply(Status.OK, executionCount, evaluatedUserExpr);
    }

    public static ShellExecuteReply withError(int executionCount) {
        return new ShellExecuteReply(Status.ERROR, executionCount, null);
    }

    @Override
    public MessageType<ShellExecuteReply> type() {
        return MessageType.SHELL_EXECUTE_REPLY;
    }

    public enum Status {
        @SerializedName("ok") OK,
        @SerializedName("error") ERROR
    }
}
