package org.rapaio.jupyter.kernel.message.messages;

import java.util.List;

import org.rapaio.jupyter.kernel.core.format.OutputFormatter;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

import com.google.gson.annotations.SerializedName;

public record ErrorReply(
        @SerializedName("status") String status,
        @SerializedName("ename") String errName,
        @SerializedName("evalue") String errMsg,
        @SerializedName("traceback") List<String> traceback,
        @SerializedName("execution_count") Integer executionCount) implements ContentType<Object> {

    public MessageType<Object> type() {
        return MessageType.UNKNOWN;
    }

    public static ErrorReply of(JavaEngine javaEngine, Exception exception, int executionCount) {

        String name = exception.getClass().getSimpleName();
        String msg = exception.getLocalizedMessage();

        return new ErrorReply(name, msg == null ? "" : msg, OutputFormatter.exceptionFormat(javaEngine, exception), executionCount);
    }

    public ErrorReply(String errName, String errMsg, List<String> stacktrace, int executionCount) {
        this("error", errName, errMsg, stacktrace, executionCount);
    }
}
