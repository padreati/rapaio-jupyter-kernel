package org.rapaio.jupyter.kernel.message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.rapaio.jupyter.kernel.message.messages.CustomCommClose;
import org.rapaio.jupyter.kernel.message.messages.CustomCommMsg;
import org.rapaio.jupyter.kernel.message.messages.CustomCommOpen;
import org.rapaio.jupyter.kernel.message.messages.IOPubClearOutput;
import org.rapaio.jupyter.kernel.message.messages.IOPubDisplayData;
import org.rapaio.jupyter.kernel.message.messages.IOPubError;
import org.rapaio.jupyter.kernel.message.messages.IOPubExecuteInput;
import org.rapaio.jupyter.kernel.message.messages.IOPubExecuteResult;
import org.rapaio.jupyter.kernel.message.messages.IOPubStatus;
import org.rapaio.jupyter.kernel.message.messages.IOPubStream;
import org.rapaio.jupyter.kernel.message.messages.IOPubUpdateDisplayData;
import org.rapaio.jupyter.kernel.message.messages.ShellCommInfoReply;
import org.rapaio.jupyter.kernel.message.messages.ShellCompleteReply;
import org.rapaio.jupyter.kernel.message.messages.ErrorReply;
import org.rapaio.jupyter.kernel.message.messages.ShellExecuteReply;
import org.rapaio.jupyter.kernel.message.messages.ShellHistoryReply;
import org.rapaio.jupyter.kernel.message.messages.StdinInputReply;
import org.rapaio.jupyter.kernel.message.messages.ShellInspectReply;
import org.rapaio.jupyter.kernel.message.messages.ControlInterruptReply;
import org.rapaio.jupyter.kernel.message.messages.ShellIsCompleteReply;
import org.rapaio.jupyter.kernel.message.messages.ShellKernelInfoReply;
import org.rapaio.jupyter.kernel.message.messages.ControlShutdownReply;
import org.rapaio.jupyter.kernel.message.messages.ShellCommInfoRequest;
import org.rapaio.jupyter.kernel.message.messages.ShellCompleteRequest;
import org.rapaio.jupyter.kernel.message.messages.ShellExecuteRequest;
import org.rapaio.jupyter.kernel.message.messages.ShellHistoryRequest;
import org.rapaio.jupyter.kernel.message.messages.StdinInputRequest;
import org.rapaio.jupyter.kernel.message.messages.ShellInspectRequest;
import org.rapaio.jupyter.kernel.message.messages.ControlInterruptRequest;
import org.rapaio.jupyter.kernel.message.messages.ShellIsCompleteRequest;
import org.rapaio.jupyter.kernel.message.messages.ShellKernelInfoRequest;
import org.rapaio.jupyter.kernel.message.messages.ControlShutdownRequest;

/**
 * Message type definitions are found at
 * <a href="https://jupyter-client.readthedocs.io/en/stable/messaging.html">jupyter_client 8.2.0 documentation</a>
 *
 * @param <T> Type class which describes the message content
 */
public class MessageType<T> {

    private static final AtomicInteger ID = new AtomicInteger(0);
    private static final Map<String, MessageType<?>> TYPE_REGISTRY = new HashMap<>();

    public static final MessageType<ShellExecuteRequest> SHELL_EXECUTE_REQUEST =
            new MessageType<>("execute_request", ShellExecuteRequest.class);

    public static final MessageType<ShellExecuteReply> SHELL_EXECUTE_REPLY =
            new MessageType<>("execute_reply", ShellExecuteReply.class);

    public static final MessageType<ShellInspectRequest> SHELL_INSPECT_REQUEST =
            new MessageType<>("inspect_request", ShellInspectRequest.class);

    public static final MessageType<ShellInspectReply> SHELL_INSPECT_REPLY =
            new MessageType<>("inspect_reply", ShellInspectReply.class);

    public static final MessageType<ShellCompleteRequest> SHELL_COMPLETE_REQUEST =
            new MessageType<>("complete_request", ShellCompleteRequest.class);

    public static final MessageType<ShellCompleteReply> SHELL_COMPLETE_REPLY =
            new MessageType<>("complete_reply", ShellCompleteReply.class);

    public static final MessageType<ShellHistoryRequest> SHELL_HISTORY_REQUEST =
            new MessageType<>("history_request", ShellHistoryRequest.class);

    public static final MessageType<ShellHistoryReply> SHELL_HISTORY_REPLY =
            new MessageType<>("history_reply", ShellHistoryReply.class);

    public static final MessageType<ShellIsCompleteRequest> SHELL_IS_COMPLETE_REQUEST =
            new MessageType<>("is_complete_request", ShellIsCompleteRequest.class);

    public static final MessageType<ShellIsCompleteReply> SHELL_IS_COMPLETE_REPLY =
            new MessageType<>("is_complete_reply", ShellIsCompleteReply.class);

    public static final MessageType<ShellCommInfoRequest> SHELL_COMM_INFO_REQUEST =
            new MessageType<>("comm_info_request", ShellCommInfoRequest.class);

    public static final MessageType<ShellCommInfoReply> SHELL_COMM_INFO_REPLY =
            new MessageType<>("comm_info_reply", ShellCommInfoReply.class);

    public static final MessageType<ShellKernelInfoRequest> SHELL_KERNEL_INFO_REQUEST =
            new MessageType<>("kernel_info_request", ShellKernelInfoRequest.class);

    public static final MessageType<ShellKernelInfoReply> SHELL_KERNEL_INFO_REPLY =
            new MessageType<>("kernel_info_reply", ShellKernelInfoReply.class);



    public static final MessageType<ControlShutdownRequest> CONTROL_SHUTDOWN_REQUEST =
            new MessageType<>("shutdown_request", ControlShutdownRequest.class);

    public static final MessageType<ControlShutdownReply> CONTROL_SHUTDOWN_REPLY =
            new MessageType<>("shutdown_reply", ControlShutdownReply.class);

    public static final MessageType<ControlInterruptRequest> CONTROL_INTERRUPT_REQUEST =
            new MessageType<>("interrupt_request", ControlInterruptRequest.class);

    public static final MessageType<ControlInterruptReply> CONTROL_INTERRUPT_REPLY =
            new MessageType<>("interrupt_reply", ControlInterruptReply.class);



    public static final MessageType<IOPubStream> IOPUB_STREAM =
            new MessageType<>("stream", IOPubStream.class);

    public static final MessageType<IOPubDisplayData> IOPUB_DISPLAY_DATA =
            new MessageType<>("display_data", IOPubDisplayData.class);

    public static final MessageType<IOPubUpdateDisplayData> IOPUB_UPDATE_DISPLAY_DATA =
            new MessageType<>("update_display_data", IOPubUpdateDisplayData.class);

    public static final MessageType<IOPubExecuteInput> IOPUB_EXECUTE_INPUT =
            new MessageType<>("execute_input", IOPubExecuteInput.class);

    public static final MessageType<IOPubExecuteResult> IOPUB_EXECUTE_RESULT =
            new MessageType<>("execute_result", IOPubExecuteResult.class);

    public static final MessageType<IOPubError> IOPUB_ERROR =
            new MessageType<>("error", IOPubError.class);

    public static final MessageType<IOPubStatus> IOPUB_STATUS =
            new MessageType<>("status", IOPubStatus.class);

    public static final MessageType<IOPubClearOutput> IOPUB_CLEAR_OUTPUT =
            new MessageType<>("clear_output", IOPubClearOutput.class);


    public static final MessageType<StdinInputRequest> STDIN_INPUT_REQUEST =
            new MessageType<>("input_request", StdinInputRequest.class);

    public static final MessageType<StdinInputReply> STDIN_INPUT_REPLY =
            new MessageType<>("input_reply", StdinInputReply.class);


    public static final MessageType<CustomCommOpen> CUSTOM_COMM_OPEN =
            new MessageType<>("comm_open", CustomCommOpen.class);

    public static final MessageType<CustomCommMsg> CUSTOM_COMM_MSG =
            new MessageType<>("comm_msg", CustomCommMsg.class);

    public static final MessageType<CustomCommClose> CUSTOM_COMM_CLOSE =
            new MessageType<>("comm_close", CustomCommClose.class);

    public static final MessageType<Object> UNKNOWN =
            new MessageType<>("none", Object.class);




    public static MessageType<?> getType(String name) {
        if (!TYPE_REGISTRY.containsKey(name)) {
            return UNKNOWN;
        }
        return TYPE_REGISTRY.get(name);
    }

    private final int id;
    private final String name;
    private final Class<T> contentType;
    private final MessageType<ErrorReply> errorType;

    private MessageType(String name, Class<T> contentType) {
        this(name, contentType, false);
    }

    private MessageType(String name, Class<T> contentType, boolean isErrorType) {
        this.id = ID.getAndIncrement();
        this.name = name;
        this.contentType = contentType;
        if (!isErrorType) {
            TYPE_REGISTRY.put(name, this);
            this.errorType = new MessageType<>(name, ErrorReply.class, true);
        } else {
            this.errorType = null;
        }
    }

    public String getName() {
        return name;
    }

    public Class<T> getContentType() {
        return contentType;
    }

    public MessageType<ErrorReply> error() {
        return errorType;
    }

    public boolean isError() {
        return errorType == null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
