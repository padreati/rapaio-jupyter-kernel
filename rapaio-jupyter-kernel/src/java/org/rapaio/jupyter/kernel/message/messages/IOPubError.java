package org.rapaio.jupyter.kernel.message.messages;

import java.util.List;
import java.util.function.Function;

import org.rapaio.jupyter.kernel.message.ContentType;
import org.rapaio.jupyter.kernel.message.MessageType;

import com.google.gson.annotations.SerializedName;

/**
 * See also {@link ErrorReply}
 */
public record IOPubError(
        @SerializedName("ename") String errName,
        @SerializedName("evalue") String errMsg,
        @SerializedName("traceback") List<String> stacktrace) implements ContentType<IOPubError> {

    public static IOPubError of(Exception exception, Function<Exception, List<String>> formatter) {
        String name = exception.getClass().getSimpleName();
        String msg = exception.getLocalizedMessage();
        List<String> stacktrace = formatter.apply(exception);

        return new IOPubError(name, msg == null ? "" : msg, stacktrace);
    }

    @Override
    public MessageType<IOPubError> type() {
        return MessageType.IOPUB_ERROR;
    }
}
