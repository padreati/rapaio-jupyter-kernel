package org.rapaio.jupyter.kernel.message.messages;

import com.google.gson.annotations.SerializedName;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.format.ErrorFormatters;
import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

import java.util.List;

public record ErrorReply(
        @SerializedName("status") String status,
        @SerializedName("ename") String errName,
        @SerializedName("evalue") String errMsg,
        @SerializedName("traceback") List<String> traceback,
        @SerializedName("execution_count") Integer executionCount) implements ContentType<Object> {

    public MessageType<Object> type() {
        return MessageType.UNKNOWN;
    }

    public static ErrorReply of(RapaioKernel kernel, Exception exception, int executionCount) {

        String name = exception.getClass().getSimpleName();
        String msg = exception.getLocalizedMessage();

        return new ErrorReply(name, msg == null ? "" : msg, ErrorFormatters.exceptionFormat(kernel, exception), executionCount);
    }

    public ErrorReply(String errName, String errMsg, List<String> stacktrace, int executionCount) {
        this("error", errName, errMsg, stacktrace, executionCount);
    }
}
