package org.rapaio.jupyter.kernel.base.message.messages;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.rapaio.jupyter.kernel.base.message.ContentType;
import org.rapaio.jupyter.kernel.base.message.MessageType;

import com.google.gson.annotations.SerializedName;

public record ErrorReply(
        @SerializedName("status") String status,
        @SerializedName("ename") String errName,
        @SerializedName("evalue") String errMsg,
        @SerializedName("traceback") List<String> stacktrace,
        @SerializedName("execution_count") Integer executionCount) implements ContentType<Object> {

    public MessageType<Object> type() {
        return MessageType.UNKNOWN;
    }

    public static ErrorReply of(Exception exception, int executionCount) {
        String name = exception.getClass().getSimpleName();
        String msg = exception.getLocalizedMessage();
        List<String> stacktrace = Arrays.stream(exception.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.toList());

        return new ErrorReply(name, msg == null ? "" : msg, stacktrace, executionCount);
    }

    public ErrorReply(String errName, String errMsg, List<String> stacktrace, int executionCount) {
        this("error", errName, errMsg, stacktrace, executionCount);
    }
}
