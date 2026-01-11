package org.rapaio.jupyter.kernel.message;

import java.util.HashMap;
import java.util.Map;

import org.rapaio.jupyter.kernel.message.messages.ControlInterruptReply;
import org.rapaio.jupyter.kernel.message.messages.ControlInterruptRequest;
import org.rapaio.jupyter.kernel.message.messages.ControlShutdownReply;
import org.rapaio.jupyter.kernel.message.messages.ControlShutdownRequest;
import org.rapaio.jupyter.kernel.message.messages.CustomCommClose;
import org.rapaio.jupyter.kernel.message.messages.CustomCommMsg;
import org.rapaio.jupyter.kernel.message.messages.CustomCommOpen;
import org.rapaio.jupyter.kernel.message.messages.ErrorReply;
import org.rapaio.jupyter.kernel.message.messages.IOPubClearOutput;
import org.rapaio.jupyter.kernel.message.messages.IOPubDisplayData;
import org.rapaio.jupyter.kernel.message.messages.IOPubError;
import org.rapaio.jupyter.kernel.message.messages.IOPubExecuteInput;
import org.rapaio.jupyter.kernel.message.messages.IOPubExecuteResult;
import org.rapaio.jupyter.kernel.message.messages.IOPubStatus;
import org.rapaio.jupyter.kernel.message.messages.IOPubStream;
import org.rapaio.jupyter.kernel.message.messages.IOPubUpdateDisplayData;
import org.rapaio.jupyter.kernel.message.messages.ShellCommInfoReply;
import org.rapaio.jupyter.kernel.message.messages.ShellCommInfoRequest;
import org.rapaio.jupyter.kernel.message.messages.ShellCompleteReply;
import org.rapaio.jupyter.kernel.message.messages.ShellCompleteRequest;
import org.rapaio.jupyter.kernel.message.messages.ShellExecuteReply;
import org.rapaio.jupyter.kernel.message.messages.ShellExecuteRequest;
import org.rapaio.jupyter.kernel.message.messages.ShellHistoryRequest;
import org.rapaio.jupyter.kernel.message.messages.ShellInspectReply;
import org.rapaio.jupyter.kernel.message.messages.ShellInspectRequest;
import org.rapaio.jupyter.kernel.message.messages.ShellIsCompleteReply;
import org.rapaio.jupyter.kernel.message.messages.ShellIsCompleteRequest;
import org.rapaio.jupyter.kernel.message.messages.ShellKernelInfoReply;
import org.rapaio.jupyter.kernel.message.messages.ShellKernelInfoRequest;
import org.rapaio.jupyter.kernel.message.messages.StdinInputReply;
import org.rapaio.jupyter.kernel.message.messages.StdinInputRequest;

/**
 * Message type definitions are found at
 * <a href="https://jupyter-client.readthedocs.io/en/stable/messaging.html">jupyter_client 8.2.0 documentation</a>
 *
 * @param <T> Type class which describes the message content
 */
public record MessageType<T>(String name, Class<T> contentType) {

    private static final Map<String, MessageType<?>> REGISTRY = new HashMap<>();

    public static final MessageType<Object> UNKNOWN;
    public static final MessageType<ShellExecuteRequest> SHELL_EXECUTE_REQUEST;
    public static final MessageType<ShellExecuteReply> SHELL_EXECUTE_REPLY;
    public static final MessageType<ShellInspectRequest> SHELL_INSPECT_REQUEST;
    public static final MessageType<ShellInspectReply> SHELL_INSPECT_REPLY;
    public static final MessageType<ShellCompleteRequest> SHELL_COMPLETE_REQUEST;
    public static final MessageType<ShellCompleteReply> SHELL_COMPLETE_REPLY;
    public static final MessageType<ShellHistoryRequest> SHELL_HISTORY_REQUEST;
    public static final MessageType<ShellIsCompleteRequest> SHELL_IS_COMPLETE_REQUEST;
    public static final MessageType<ShellIsCompleteReply> SHELL_IS_COMPLETE_REPLY;
    public static final MessageType<ShellCommInfoRequest> SHELL_COMM_INFO_REQUEST;
    public static final MessageType<ShellCommInfoReply> SHELL_COMM_INFO_REPLY;
    public static final MessageType<ShellKernelInfoRequest> SHELL_KERNEL_INFO_REQUEST;
    public static final MessageType<ShellKernelInfoReply> SHELL_KERNEL_INFO_REPLY;
    public static final MessageType<ControlShutdownRequest> CONTROL_SHUTDOWN_REQUEST;
    public static final MessageType<ControlShutdownReply> CONTROL_SHUTDOWN_REPLY;
    public static final MessageType<ControlInterruptRequest> CONTROL_INTERRUPT_REQUEST;
    public static final MessageType<ControlInterruptReply> CONTROL_INTERRUPT_REPLY;
    public static final MessageType<IOPubStream> IOPUB_STREAM;
    public static final MessageType<IOPubDisplayData> IOPUB_DISPLAY_DATA;
    public static final MessageType<IOPubUpdateDisplayData> IOPUB_UPDATE_DISPLAY_DATA;
    public static final MessageType<IOPubExecuteInput> IOPUB_EXECUTE_INPUT;
    public static final MessageType<IOPubExecuteResult> IOPUB_EXECUTE_RESULT;
    public static final MessageType<IOPubError> IOPUB_ERROR;
    public static final MessageType<IOPubStatus> IOPUB_STATUS;
    public static final MessageType<IOPubClearOutput> IOPUB_CLEAR_OUTPUT;
    public static final MessageType<StdinInputRequest> STDIN_INPUT_REQUEST;
    public static final MessageType<StdinInputReply> STDIN_INPUT_REPLY;
    public static final MessageType<CustomCommOpen> CUSTOM_COMM_OPEN;
    public static final MessageType<CustomCommMsg> CUSTOM_COMM_MSG;
    public static final MessageType<CustomCommClose> CUSTOM_COMM_CLOSE;


    static {
        UNKNOWN = register(new MessageType<>("none", Object.class));
        SHELL_EXECUTE_REQUEST = register(new MessageType<>("execute_request", ShellExecuteRequest.class));
        SHELL_EXECUTE_REPLY = register(new MessageType<>("execute_reply", ShellExecuteReply.class));
        SHELL_INSPECT_REQUEST = register(new MessageType<>("inspect_request", ShellInspectRequest.class));
        SHELL_INSPECT_REPLY = register(new MessageType<>("inspect_reply", ShellInspectReply.class));
        SHELL_COMPLETE_REQUEST = register(new MessageType<>("complete_request", ShellCompleteRequest.class));
        SHELL_COMPLETE_REPLY = register(new MessageType<>("complete_reply", ShellCompleteReply.class));
        SHELL_HISTORY_REQUEST = register(new MessageType<>("history_request", ShellHistoryRequest.class));
        SHELL_IS_COMPLETE_REQUEST = register(new MessageType<>("is_complete_request", ShellIsCompleteRequest.class));
        SHELL_IS_COMPLETE_REPLY = register(new MessageType<>("is_complete_reply", ShellIsCompleteReply.class));
        SHELL_COMM_INFO_REQUEST = register(new MessageType<>("comm_info_request", ShellCommInfoRequest.class));
        SHELL_COMM_INFO_REPLY = register(new MessageType<>("comm_info_reply", ShellCommInfoReply.class));
        SHELL_KERNEL_INFO_REQUEST = register(new MessageType<>("kernel_info_request", ShellKernelInfoRequest.class));
        SHELL_KERNEL_INFO_REPLY = register(new MessageType<>("kernel_info_reply", ShellKernelInfoReply.class));
        CONTROL_SHUTDOWN_REQUEST = register(new MessageType<>("shutdown_request", ControlShutdownRequest.class));
        CONTROL_SHUTDOWN_REPLY = register(new MessageType<>("shutdown_reply", ControlShutdownReply.class));
        CONTROL_INTERRUPT_REQUEST = register(new MessageType<>("interrupt_request", ControlInterruptRequest.class));
        CONTROL_INTERRUPT_REPLY = register(new MessageType<>("interrupt_reply", ControlInterruptReply.class));
        IOPUB_STREAM = register(new MessageType<>("stream", IOPubStream.class));
        IOPUB_DISPLAY_DATA = register(new MessageType<>("display_data", IOPubDisplayData.class));
        IOPUB_UPDATE_DISPLAY_DATA = register(new MessageType<>("update_display_data", IOPubUpdateDisplayData.class));
        IOPUB_EXECUTE_INPUT = register(new MessageType<>("execute_input", IOPubExecuteInput.class));
        IOPUB_EXECUTE_RESULT = register(new MessageType<>("execute_result", IOPubExecuteResult.class));
        IOPUB_ERROR = register(new MessageType<>("error", IOPubError.class));
        IOPUB_STATUS = register(new MessageType<>("status", IOPubStatus.class));
        IOPUB_CLEAR_OUTPUT = register(new MessageType<>("clear_output", IOPubClearOutput.class));
        STDIN_INPUT_REQUEST = register(new MessageType<>("input_request", StdinInputRequest.class));
        STDIN_INPUT_REPLY = register(new MessageType<>("input_reply", StdinInputReply.class));
        CUSTOM_COMM_OPEN = register(new MessageType<>("comm_open", CustomCommOpen.class));
        CUSTOM_COMM_MSG = register(new MessageType<>("comm_msg", CustomCommMsg.class));
        CUSTOM_COMM_CLOSE = register(new MessageType<>("comm_close", CustomCommClose.class));
    }

    private static <MT> MessageType<MT> register(MessageType<MT> messageType) {
        REGISTRY.put(messageType.name, messageType);
        return messageType;
    }

    public static MessageType<?> getType(String name) {
        if (!REGISTRY.containsKey(name)) {
            return UNKNOWN;
        }
        return REGISTRY.get(name);
    }

    public MessageType<ErrorReply> newError() {
        return new MessageType<>(name, ErrorReply.class);
    }
}
