package org.rapaio.jupyter.kernel.core;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.rapaio.jupyter.kernel.channels.JupyterChannels;
import org.rapaio.jupyter.kernel.channels.KernelMessageHandler;
import org.rapaio.jupyter.kernel.channels.MessageHandler;
import org.rapaio.jupyter.kernel.channels.ReplyEnv;
import org.rapaio.jupyter.kernel.core.display.DefaultRenderer;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.Renderer;
import org.rapaio.jupyter.kernel.core.java.CompileException;
import org.rapaio.jupyter.kernel.core.java.EvaluationInterruptedException;
import org.rapaio.jupyter.kernel.core.java.EvaluationTimeoutException;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEvaluator;
import org.rapaio.jupyter.kernel.core.magic.MagicParseException;
import org.rapaio.jupyter.kernel.message.Header;
import org.rapaio.jupyter.kernel.message.Message;
import org.rapaio.jupyter.kernel.message.MessageType;
import org.rapaio.jupyter.kernel.message.messages.ControlInterruptReply;
import org.rapaio.jupyter.kernel.message.messages.ControlInterruptRequest;
import org.rapaio.jupyter.kernel.message.messages.ControlShutdownReply;
import org.rapaio.jupyter.kernel.message.messages.ControlShutdownRequest;
import org.rapaio.jupyter.kernel.message.messages.CustomCommClose;
import org.rapaio.jupyter.kernel.message.messages.CustomCommMsg;
import org.rapaio.jupyter.kernel.message.messages.CustomCommOpen;
import org.rapaio.jupyter.kernel.message.messages.ErrorReply;
import org.rapaio.jupyter.kernel.message.messages.IOPubError;
import org.rapaio.jupyter.kernel.message.messages.IOPubExecuteInput;
import org.rapaio.jupyter.kernel.message.messages.IOPubExecuteResult;
import org.rapaio.jupyter.kernel.message.messages.ShellCommInfoReply;
import org.rapaio.jupyter.kernel.message.messages.ShellCommInfoRequest;
import org.rapaio.jupyter.kernel.message.messages.ShellExecuteReply;
import org.rapaio.jupyter.kernel.message.messages.ShellExecuteRequest;
import org.rapaio.jupyter.kernel.message.messages.ShellKernelInfoReply;
import org.rapaio.jupyter.kernel.message.messages.ShellKernelInfoRequest;

public class RapaioKernel implements KernelMessageHandler {

    private static final AtomicInteger executionCount = new AtomicInteger(1);

    private final Map<MessageType, MessageHandler> messageHandlers = new HashMap<>();
    private JupyterChannels channels;

    private final Renderer renderer;
    private final JavaEngine javaEngine;
    private final MagicEvaluator magicEvaluator;

    public RapaioKernel() {

        this.renderer = new DefaultRenderer();
        this.javaEngine = JavaEngine.builder()
                .withTimeoutMillis(-1L)
                .build();
        this.magicEvaluator = new MagicEvaluator(javaEngine);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MessageHandler<T> getHandler(MessageType<T> type) {
        return messageHandlers.get(type);
    }

    private ShellKernelInfoReply getKernelInfo() {

        String kernelName = "rapaio-jupyter-kernel";
        String kernelVersion = "0.1.0";
        LanguageInfo languageInfo = LanguageInfo.kernelLanguageInfo();
        String banner = languageInfo.name() + " " + languageInfo.version();

        List<LanguageInfo.Help> helpLinks = List.of(
                new LanguageInfo.Help("rapaio-jupyter-kernel homepage", "https://github.com/padreati/rapaio-jupyter-kernel")
        );

        return new ShellKernelInfoReply(Header.PROTOCOL_VERSION, kernelName, kernelVersion, languageInfo, banner, helpLinks);
    }

    /**
     * Invoked on shutdown requests with messages, before shutting down the connection.
     *
     * @param restart if it will be followed by a restart
     */
    public void onShutdown(boolean restart) {
        javaEngine.shutdown(restart);
    }

    /**
     * Invoked when the client requested to interrupt the code executor.
     */
    public void interrupt() {
        javaEngine.interrupt();
    }

    public DisplayData eval(String expr) throws Exception {

        // try first the magic
        MagicEvaluator.MagicResult magicResult = magicEvaluator.eval(expr);
        if (magicResult.handled()) {
            return transformEval(magicResult.result());
        }

        // if not handled try the java engine
        return transformEval(javaEngine.eval(expr));
    }

    private DisplayData transformEval(Object result) {
        if (result != null) {
            if (result instanceof DisplayData dd) {
                return dd;
            }
            return renderer.render(result);
        }
        return null;
    }

    public List<String> formatException(Exception e) {
        if (e instanceof CompileException ce) {
            return formatCompileException(ce);
        }
        if (e instanceof EvaluationInterruptedException ie) {
            return formatInterruptedException(ie);
        }
        if (e instanceof EvaluationTimeoutException te) {
            return formatTimeoutException(te);
        }
        if (e instanceof MagicParseException me) {
            return formatMagicParseExpression(me);
        }
        return List.of(e.getMessage());
    }

    private List<String> formatCompileException(CompileException e) {
        // todo: something nicer
        return List.of("CompileExceptio:", e.getBadSnippetCompilation().toString());
    }

    private List<String> formatInterruptedException(EvaluationInterruptedException e) {
        // todo: something better
        return List.of("InterruptedException:", e.getSource());
    }

    private List<String> formatTimeoutException(EvaluationTimeoutException e) {
        // todo: something better
        return List.of(e.getMessage());
    }

    private List<String> formatMagicParseExpression(MagicParseException e) {
        // todo: something better
        return List.of(e.getMessage());
    }

    //
    // MESSAGE HANDLERS
    //

    @Override
    public void registerChannels(JupyterChannels channels) {
        this.channels = channels;
        messageHandlers.put(MessageType.SHELL_EXECUTE_REQUEST, this::handleExecuteRequest);
//        messageHandlers.put(MessageType.SHELL_INSPECT_REQUEST, this::handleInspectRequest);
//        messageHandlers.put(MessageType.SHELL_COMPLETE_REQUEST, this::handleCompleteRequest);
//        messageHandlers.put(MessageType.SHELL_HISTORY_REQUEST, this::handleHistoryRequest);
//        messageHandlers.put(MessageType.SHELL_IS_COMPLETE_REQUEST, this::handleIsCodeCompeteRequest);
        messageHandlers.put(MessageType.SHELL_KERNEL_INFO_REQUEST, this::handleKernelInfoRequest);
        messageHandlers.put(MessageType.CONTROL_SHUTDOWN_REQUEST, this::handleShutdownRequest);
        messageHandlers.put(MessageType.CONTROL_INTERRUPT_REQUEST, this::handleInterruptRequest);

        messageHandlers.put(MessageType.CUSTOM_COMM_OPEN, this::handleCommOpenCommand);
        messageHandlers.put(MessageType.CUSTOM_COMM_MSG, this::handleCommMsgCommand);
        messageHandlers.put(MessageType.CUSTOM_COMM_CLOSE, this::handleCommCloseCommand);
        messageHandlers.put(MessageType.SHELL_COMM_INFO_REQUEST, this::handleCommInfoRequest);
    }

    private void handleExecuteRequest(ReplyEnv env, Message<ShellExecuteRequest> executeRequestMessage) {
        ShellExecuteRequest request = executeRequestMessage.content();

        int count = executionCount.getAndIncrement();

        env.setBusyDeferIdle();
        env.publish(new IOPubExecuteInput(request.code(), count));

        // collect old streams
        PrintStream oldStdOut = System.out;
        PrintStream oldStdErr = System.err;
        InputStream oldStdIn = System.in;

        // some clients don't allow asking input
        boolean allowStdin = executeRequestMessage.content().stdinEnabled();
        // hooke system io to allow execution to output into notebook
        env.interceptSystemIO(allowStdin);

        // push on stack restore streams
        env.defer(() -> {
            System.setOut(oldStdOut);
            System.setErr(oldStdErr);
            System.setIn(oldStdIn);
        });

        // flush before restoring
        env.defer(env::flushSystemIO);

        try {
            DisplayData out = eval(request.code());

            if (out != null) {
                env.publish(new IOPubExecuteResult(count, out));
            }

            env.defer().reply(ShellExecuteReply.withOk(count, Collections.emptyMap()));
        } catch (Exception e) {
            ErrorReply error = ErrorReply.of(e, count);
            env.publish(IOPubError.of(e, this::formatException));
            env.defer().replyError(MessageType.SHELL_EXECUTE_REPLY.error(), error);
        }
    }

    private void handleKernelInfoRequest(ReplyEnv env, Message<ShellKernelInfoRequest> ignored) {
        env.setBusyDeferIdle();
        env.reply(getKernelInfo());
    }

    private void handleShutdownRequest(ReplyEnv env, Message<ControlShutdownRequest> shutdownRequestMessage) {
        ControlShutdownRequest request = shutdownRequestMessage.content();
        env.setBusyDeferIdle();

        env.defer().reply(request.restart() ? ControlShutdownReply.SHUTDOWN_AND_RESTART : ControlShutdownReply.SHUTDOWN);

        onShutdown(request.restart());
        env.resolveDeferrals();
        // this will determine the connections to shut down
        env.markForShutdown();
    }

    private void handleInterruptRequest(ReplyEnv env, Message<ControlInterruptRequest> interruptRequestMessage) {
        env.setBusyDeferIdle();
        env.reply(new ControlInterruptReply());
        interrupt();
    }

    // custom communication is not implemented, however, we have to behave properly

    private void handleCommOpenCommand(ReplyEnv env, Message<CustomCommOpen> message) {
        CustomCommOpen openCommand = message.content();
        env.setBusyDeferIdle();
        CustomCommClose closeCommand = new CustomCommClose(openCommand.commId(), Transform.EMPTY_JSON_OBJ);
        env.publish(closeCommand);
    }

    private void handleCommMsgCommand(ReplyEnv env, Message<CustomCommMsg> ignored) {
        // no-op
    }

    private void handleCommCloseCommand(ReplyEnv env, Message<CustomCommClose> ignored) {
        // no-op
    }

    private void handleCommInfoRequest(ReplyEnv env, Message<ShellCommInfoRequest> message) {
        env.setBusyDeferIdle();
        Map<String, ShellCommInfoReply.CommInfo> comms = new LinkedHashMap<>();
        env.reply(new ShellCommInfoReply(comms));
    }
}
