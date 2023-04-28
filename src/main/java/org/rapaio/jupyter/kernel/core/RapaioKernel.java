package org.rapaio.jupyter.kernel.core;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.rapaio.jupyter.kernel.channels.JupyterChannels;
import org.rapaio.jupyter.kernel.channels.KernelMessageHandler;
import org.rapaio.jupyter.kernel.channels.MessageHandler;
import org.rapaio.jupyter.kernel.channels.ReplyEnv;
import org.rapaio.jupyter.kernel.core.display.DefaultRenderer;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.Renderer;
import org.rapaio.jupyter.kernel.core.display.text.ANSIText;
import org.rapaio.jupyter.kernel.core.java.CompilerException;
import org.rapaio.jupyter.kernel.core.java.EvaluationInterruptedException;
import org.rapaio.jupyter.kernel.core.java.EvaluationTimeoutException;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.java.io.JShellIO;
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
import org.rapaio.jupyter.kernel.message.messages.IOPubDisplayData;
import org.rapaio.jupyter.kernel.message.messages.IOPubError;
import org.rapaio.jupyter.kernel.message.messages.IOPubExecuteInput;
import org.rapaio.jupyter.kernel.message.messages.IOPubExecuteResult;
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

import jdk.jshell.DeclarationSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;

@SuppressWarnings("rawtypes")
public class RapaioKernel implements KernelMessageHandler {

    public static final String SHELL_INIT_RESOURCE_PATH = "init.jshell";

    private static final Logger LOGGER = Logger.getLogger(RapaioKernel.class.getSimpleName());
    private static final AtomicInteger executionCount = new AtomicInteger(1);

    private final Map<MessageType<?>, MessageHandler> messageHandlers = new HashMap<>();
    private JupyterChannels channels;

    private final Renderer renderer;
    private final JavaEngine javaEngine;
    private final MagicEvaluator magicEvaluator;

    private ReplyEnv currentReplyEnv;
    private final JShellIO shellIO = new JShellIO();

    public RapaioKernel() {

        this.renderer = new DefaultRenderer();
        this.javaEngine = JavaEngine.builder()
                .withStartupScript(RapaioKernel.class.getClassLoader().getResourceAsStream(SHELL_INIT_RESOURCE_PATH))
                .withTimeoutMillis(-1L)
                .build();
        this.javaEngine.initialize();
        this.magicEvaluator = new MagicEvaluator(javaEngine);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MessageHandler<T> getHandler(MessageType<T> type) {
        return messageHandlers.get(type);
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public void display(DisplayData dd) {
        if (currentReplyEnv != null) {
            currentReplyEnv.publish(new IOPubDisplayData(dd));
        }
    }

    public void updateDisplay(String id, DisplayData dd) {
        dd.setDisplayId(id);
        updateDisplay(dd);
    }

    public void updateDisplay(DisplayData dd) {
        if (currentReplyEnv != null) {
            if (!dd.hasDisplayId()) {
                throw new IllegalArgumentException("Cannot update a display without display_id.");
            }
            currentReplyEnv.publish(new IOPubUpdateDisplayData(dd));
        }
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
     */
    public void onShutdown() {
        javaEngine.shutdown();
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
        if (e instanceof CompilerException ce) {
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

    private List<String> formatCompileException(CompilerException e) {

        List<String> msgs = new ArrayList<>(ANSIText.errorTypeHeader("Compile error"));
        SnippetEvent event = e.getBadSnippetCompilation();
        Snippet snippet = event.snippet();
        var diagnostics = javaEngine.getShell().diagnostics(snippet).toList();
        for (var d : diagnostics) {
            msgs.addAll(ANSIText.sourceCode(snippet.source(), (int) d.getPosition(),
                    (int) d.getStartPosition(), (int) d.getEndPosition()));

            msgs.addAll(ANSIText.errorMessages(d.getMessage(Locale.getDefault())));
            msgs.add("");
        }
        // Declaration snippets are unique in that they can be active with unresolved references
        if (snippet instanceof DeclarationSnippet declarationSnippet) {
            List<String> unresolvedDependencies = javaEngine.getShell().unresolvedDependencies(declarationSnippet).toList();
            if (!unresolvedDependencies.isEmpty()) {
                msgs.addAll(ANSIText.sourceCode(snippet.source()));
                msgs.addAll(ANSIText.errorMessages("Unresolved dependencies:"));
                unresolvedDependencies.forEach(dep -> msgs.addAll(ANSIText.errorMessages("   - " + dep)));
            }
        }

        return msgs;
    }

    private List<String> formatInterruptedException(EvaluationInterruptedException e) {
        List<String> msgs = new ArrayList<>(ANSIText.errorTypeHeader("InterruptedException"));
        msgs.addAll(ANSIText.sourceCode(e.getSource()));
        return msgs;
    }

    private List<String> formatTimeoutException(EvaluationTimeoutException e) {
        List<String> msgs = new ArrayList<>(ANSIText.errorTypeHeader("TimeoutException"));
        msgs.addAll(ANSIText.sourceCode(e.getSource()));
        msgs.addAll(ANSIText.errorMessages(e.getMessage()));
        return msgs;
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
        messageHandlers.put(MessageType.SHELL_INSPECT_REQUEST, this::handleInspectRequest);
        messageHandlers.put(MessageType.SHELL_COMPLETE_REQUEST, this::handleCompleteRequest);
        messageHandlers.put(MessageType.SHELL_HISTORY_REQUEST, this::handleHistoryRequest);
        messageHandlers.put(MessageType.SHELL_IS_COMPLETE_REQUEST, this::handleIsCompleteRequest);
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
        // hook system io to allow execution to output into notebook
        System.setIn(shellIO.getIn());
        System.setOut(new PrintStream(shellIO.getOut(), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(shellIO.getErr(), true, StandardCharsets.UTF_8));

        shellIO.bindEnv(env, allowStdin);

        currentReplyEnv = env;

        env.defer(() -> currentReplyEnv = null);

        // push on stack restore streams
        env.defer(() -> {
            System.setOut(oldStdOut);
            System.setErr(oldStdErr);
            System.setIn(oldStdIn);
        });

        // unbind environment
        env.defer(shellIO::unbindEnv);
        // flush before restoring
        env.defer(shellIO::flush);

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

    public static final String IS_COMPLETE_STATUS_YES = "complete";
    public static final String IS_COMPLETE_STATUS_BAD = "invalid";
    public static final String IS_COMPLETE_STATUS_MAYBE = "unknown";

    private void handleIsCompleteRequest(ReplyEnv env, Message<ShellIsCompleteRequest> message) {
        ShellIsCompleteRequest request = message.content();
        env.setBusyDeferIdle();

        String result = javaEngine.isComplete(request.code());

        ShellIsCompleteReply reply = switch (result) {
            case IS_COMPLETE_STATUS_YES -> ShellIsCompleteReply.VALID_CODE;
            case IS_COMPLETE_STATUS_BAD -> ShellIsCompleteReply.INVALID_CODE;
            case IS_COMPLETE_STATUS_MAYBE -> ShellIsCompleteReply.UNKNOWN;
            default -> ShellIsCompleteReply.getIncompleteReplyWithIndent(result);
        };
        env.reply(reply);

    }

    private void handleInspectRequest(ReplyEnv env, Message<ShellInspectRequest> message) {
        ShellInspectRequest request = message.content();
        env.setBusyDeferIdle();
        try {
            // request.detailLevel() is not used since we do not get the source as required
            // for detail level above 0
            DisplayData inspection = javaEngine.inspect(request.code(), request.cursorPos());
            env.reply(new ShellInspectReply(inspection != null, DisplayData.emptyIfNull(inspection)));
        } catch (Exception e) {
            env.replyError(MessageType.SHELL_INSPECT_REPLY.error(), ErrorReply.of(e, 0));
        }
    }

    private void handleCompleteRequest(ReplyEnv env, Message<ShellCompleteRequest> message) {
        ShellCompleteRequest request = message.content();
        env.setBusyDeferIdle();
        try {
            ReplacementOptions options = javaEngine.complete(request.code(), request.cursorPos());
            if (options == null) {
                env.reply(new ShellCompleteReply(Collections.emptyList(), request.cursorPos(), request.cursorPos(),
                        Collections.emptyMap()));
            } else {
                env.reply(new ShellCompleteReply(options.replacements(), options.start(), options.end(),
                        Collections.emptyMap()));
            }
        } catch (Exception e) {
            env.replyError(MessageType.SHELL_COMPLETE_REPLY.error(), ErrorReply.of(e, 0));
        }
    }

    private void handleHistoryRequest(ReplyEnv env, Message<ShellHistoryRequest> message) {
        /*
        Note from specifications:
        Most of the history messaging options are not used by Jupyter frontends, and many kernels do not implement them.
        If you’re implementing these messages in a kernel, the ‘tail’ request is the most useful;
        this is used by the Qt console, for example.
        The notebook interface does not use history messages at all.
         */
        LOGGER.info("ShellHistoryRequest not implemented.");
    }

    private void handleKernelInfoRequest(ReplyEnv env, Message<ShellKernelInfoRequest> ignored) {
        env.setBusyDeferIdle();
        env.reply(getKernelInfo());
    }

    private void handleShutdownRequest(ReplyEnv env, Message<ControlShutdownRequest> shutdownRequestMessage) {
        ControlShutdownRequest request = shutdownRequestMessage.content();
        env.setBusyDeferIdle();

        env.defer().reply(request.restart() ? ControlShutdownReply.SHUTDOWN_AND_RESTART : ControlShutdownReply.SHUTDOWN);

        // request.restart() is no use for now, but might be used in the end
        onShutdown();
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
