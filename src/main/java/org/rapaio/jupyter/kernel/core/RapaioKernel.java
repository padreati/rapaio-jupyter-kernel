package org.rapaio.jupyter.kernel.core;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.channels.MessageHandler;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.Renderer;
import org.rapaio.jupyter.kernel.core.format.OutputFormatter;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.java.io.JShellConsole;
import org.rapaio.jupyter.kernel.core.magic.MagicCompleteResult;
import org.rapaio.jupyter.kernel.core.magic.MagicEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalResult;
import org.rapaio.jupyter.kernel.core.magic.MagicInspectResult;
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

@SuppressWarnings("rawtypes")
public class RapaioKernel {

    public static final String RJK_TIMEOUT_MILLIS = "RJK_TIMEOUT_MILLIS";
    public static final String RJK_COMPILER_OPTIONS = "RJK_COMPILER_OPTIONS";
    public static final String RJK_INIT_SCRIPT = "RJK_INIT_SCRIPT";

    public static final String DEFAULT_RJK_TIMEOUT_MILLIS = "-1";
    public static final String DEFAULT_COMPILER_OPTIONS = "";
    public static final String DEFAULT_INIT_SCRIPT = "";

    private static final String SHELL_INIT_RESOURCE_PATH = "init.jshell";

    private static final Logger LOGGER = Logger.getLogger(RapaioKernel.class.getSimpleName());
    private static final AtomicInteger executionCount = new AtomicInteger(1);

    private final Renderer renderer;
    private final JavaEngine javaEngine;
    private final MagicEngine magicEngine;
    private final JShellConsole shellConsole;

    private Channels channels;

    public RapaioKernel() {

        KernelEnv kernelEnv = new KernelEnv();

        this.shellConsole = new JShellConsole();
        this.javaEngine = JavaEngine.builder(shellConsole)
                .withCompilerOptions(kernelEnv.compilerOptions())
                .withStartupScript(RapaioKernel.class.getClassLoader().getResourceAsStream(SHELL_INIT_RESOURCE_PATH))
                .withStartupScript(kernelEnv.initScriptContent())
                .withTimeoutMillis(kernelEnv.timeoutMillis())
                .build();
        this.javaEngine.initialize();
        this.magicEngine = new MagicEngine(javaEngine);
        this.renderer = new Renderer();
    }


    public Channels getChannels() {
        return channels;
    }

    public JavaEngine getJavaEngine() {
        return javaEngine;
    }

    public MagicEngine getMagicEngine() {
        return magicEngine;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public void display(DisplayData dd) {
        if (channels.hasContext()) {
            channels.publish(new IOPubDisplayData(dd));
        }
    }

    public void updateDisplay(String id, DisplayData dd) {
        dd.setDisplayId(id);
        updateDisplay(dd);
    }

    public void updateDisplay(DisplayData dd) {
        if (channels.hasContext()) {
            if (!dd.hasDisplayId()) {
                throw new IllegalArgumentException("Cannot update a display without display_id.");
            }
            channels.publish(new IOPubUpdateDisplayData(dd));
        }
    }

    private ShellKernelInfoReply getKernelInfo() {

        String kernelName = "rapaio-jupyter-kernel";
        String kernelVersion = "0.2.0";
        LanguageInfo languageInfo = LanguageInfo.kernelLanguageInfo();
        String banner = languageInfo.name() + " " + languageInfo.version();

        List<LanguageInfo.Help> helpLinks = List.of(
                new LanguageInfo.Help("rapaio-jupyter-kernel homepage", "https://github.com/padreati/rapaio-jupyter-kernel")
        );

        return new ShellKernelInfoReply(Header.PROTOCOL_VERSION, kernelName, kernelVersion, languageInfo, banner, helpLinks);
    }

    public void onShutdown() {
        javaEngine.shutdown();
    }

    public void interrupt() {
        javaEngine.interrupt();
    }

    public MagicEvalResult evalMagic(String expr) throws Exception {
        return magicEngine.eval(channels, expr);
    }

    public DisplayData eval(String expr) throws Exception {
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

    public Map<MessageType<?>, MessageHandler> registerChannels(Channels channels) {
        this.channels = channels;

        Map<MessageType<?>, MessageHandler> handlers = new HashMap<>();
        handlers.put(MessageType.SHELL_EXECUTE_REQUEST, this::handleExecuteRequest);
        handlers.put(MessageType.SHELL_INSPECT_REQUEST, this::handleInspectRequest);
        handlers.put(MessageType.SHELL_COMPLETE_REQUEST, this::handleCompleteRequest);
        handlers.put(MessageType.SHELL_HISTORY_REQUEST, this::handleHistoryRequest);
        handlers.put(MessageType.SHELL_IS_COMPLETE_REQUEST, this::handleIsCompleteRequest);
        handlers.put(MessageType.SHELL_KERNEL_INFO_REQUEST, this::handleKernelInfoRequest);
        handlers.put(MessageType.CONTROL_SHUTDOWN_REQUEST, this::handleShutdownRequest);
        handlers.put(MessageType.CONTROL_INTERRUPT_REQUEST, this::handleInterruptRequest);
        handlers.put(MessageType.CUSTOM_COMM_OPEN, this::handleCommOpenCommand);
        handlers.put(MessageType.CUSTOM_COMM_MSG, this::handleCommMsgCommand);
        handlers.put(MessageType.CUSTOM_COMM_CLOSE, this::handleCommCloseCommand);
        handlers.put(MessageType.SHELL_COMM_INFO_REQUEST, this::handleCommInfoRequest);
        return handlers;
    }

    private void handleExecuteRequest(Message<ShellExecuteRequest> executeRequestMessage) {
        ShellExecuteRequest request = executeRequestMessage.content();

        int count = executionCount.getAndIncrement();

        channels.delay(channels::freeContext);
        channels.busyThenIdle();
        channels.publish(new IOPubExecuteInput(request.code(), count));

        // before binding IO try to do evaluation of magic.
        // if magic handled the request then this is it, everything remains here
        // otherwise continue normally with java evaluation
        try {
            MagicEvalResult magicResult = evalMagic(request.code());
            if (magicResult.handled()) {
                if (magicResult.result() != null) {
                    channels.publish(new IOPubExecuteResult(count, transformEval(magicResult.result())));
                }
                channels.delay(() -> channels.reply(ShellExecuteReply.withOk(count, Collections.emptyMap())));
                return;
            }
        } catch (Exception e) {
            channels.publish(IOPubError.of(e, ex -> OutputFormatter.exceptionFormat(javaEngine, ex)));
            channels.delay(() -> channels.replyError(MessageType.SHELL_EXECUTE_REPLY.newError(), ErrorReply.of(javaEngine, e, count)));
            return;
        }

        // collect old streams
        PrintStream oldStdOut = System.out;
        PrintStream oldStdErr = System.err;
        InputStream oldStdIn = System.in;

        // some clients don't allow asking input
        boolean allowStdin = executeRequestMessage.content().stdinEnabled();
        // hook system io to allow execution to output into notebook
        System.setIn(shellConsole.getIn());
        System.setOut(new PrintStream(shellConsole.getOut(), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(shellConsole.getErr(), true, StandardCharsets.UTF_8));

        shellConsole.bindChannels(channels, allowStdin);

        // push on stack restore streams
        channels.delay(() -> {
            System.setOut(oldStdOut);
            System.setErr(oldStdErr);
            System.setIn(oldStdIn);
        });

        // unbind environment
        channels.delay(shellConsole::unbindChannels);
        // flush before restoring
        channels.delay(shellConsole::flush);

        try {
            DisplayData out = eval(request.code());
            if (out != null) {
                channels.publish(new IOPubExecuteResult(count, out));
            }
            channels.delay(() -> channels.reply(ShellExecuteReply.withOk(count, Collections.emptyMap())));
        } catch (Exception e) {
            channels.publish(IOPubError.of(e, ex -> OutputFormatter.exceptionFormat(javaEngine, ex)));
            channels.delay(() -> channels.replyError(MessageType.SHELL_EXECUTE_REPLY.newError(), ErrorReply.of(javaEngine, e, count)));
        }
    }

    public static final String IS_COMPLETE_STATUS_YES = "complete";
    public static final String IS_COMPLETE_STATUS_BAD = "invalid";
    public static final String IS_COMPLETE_STATUS_MAYBE = "unknown";

    private void handleIsCompleteRequest(Message<ShellIsCompleteRequest> message) {
        ShellIsCompleteRequest request = message.content();
        channels.busyThenIdle();

        String result = javaEngine.isComplete(request.code());

        ShellIsCompleteReply reply = switch (result) {
            case IS_COMPLETE_STATUS_YES -> ShellIsCompleteReply.VALID_CODE;
            case IS_COMPLETE_STATUS_BAD -> ShellIsCompleteReply.INVALID_CODE;
            case IS_COMPLETE_STATUS_MAYBE -> ShellIsCompleteReply.UNKNOWN;
            default -> ShellIsCompleteReply.getIncompleteReplyWithIndent(result);
        };
        channels.reply(reply);
    }

    private void handleInspectRequest(Message<ShellInspectRequest> message) {
        ShellInspectRequest request = message.content();
        channels.busyThenIdle();
        try {
            MagicInspectResult magicResult = magicEngine.inspect(channels, request.code(), request.cursorPos());
            if (magicResult.handled()) {
                DisplayData inspection = magicResult.displayData();
                channels.reply(new ShellInspectReply(inspection != null, inspection));
                return;
            }

            // request.detailLevel() is not used since we do not get the source as required
            // for detail level above 0
            DisplayData inspection = javaEngine.inspect(request.code(), request.cursorPos());
            channels.reply(new ShellInspectReply(inspection != null, inspection));
        } catch (Exception e) {
            channels.replyError(MessageType.SHELL_INSPECT_REPLY.newError(), ErrorReply.of(javaEngine, e, 0));
        }
    }

    private void handleCompleteRequest(Message<ShellCompleteRequest> message) {
        ShellCompleteRequest request = message.content();
        channels.busyThenIdle();
        try {
            MagicCompleteResult magicResult = magicEngine.complete(channels, request.code(), request.cursorPos());

            CompleteMatches options = magicResult.handled()
                    ? magicResult.replacementOptions()
                    : javaEngine.complete(request.code(), request.cursorPos());

            if (options == null) {
                channels.reply(ShellCompleteReply.empty(request.cursorPos()));
            } else {
                channels.reply(ShellCompleteReply.from(options));
            }
        } catch (Exception e) {
            channels.replyError(MessageType.SHELL_COMPLETE_REPLY.newError(), ErrorReply.of(javaEngine, e, 0));
        }
    }

    private void handleHistoryRequest(Message<ShellHistoryRequest> message) {
        /*
        Note from specifications:
        Most of the history messaging options are not used by Jupyter frontends, and many kernels do not implement them.
        If you’re implementing these messages in a kernel, the ‘tail’ request is the most useful;
        this is used by the Qt console, for example.
        The notebook interface does not use history messages at all.
         */
        LOGGER.info("ShellHistoryRequest not implemented.");
    }

    private void handleKernelInfoRequest(Message<ShellKernelInfoRequest> ignored) {
        channels.busyThenIdle();
        channels.reply(getKernelInfo());
    }

    private void handleShutdownRequest(Message<ControlShutdownRequest> shutdownRequestMessage) {
        ControlShutdownRequest request = shutdownRequestMessage.content();
        channels.busyThenIdle();

        channels.delay(() -> channels.reply(request.restart() ? ControlShutdownReply.SHUTDOWN_AND_RESTART : ControlShutdownReply.SHUTDOWN));

        // request.restart() is no use for now, but might be used in the end
        onShutdown();
        channels.runDelayedActions();
        // this will determine the connections to shut down
        channels.markForShutdown();
    }

    private void handleInterruptRequest(Message<ControlInterruptRequest> interruptRequestMessage) {
        channels.busyThenIdle();
        channels.reply(new ControlInterruptReply());
        interrupt();
    }

    // custom communication is not implemented, however, we have to behave properly

    private void handleCommOpenCommand(Message<CustomCommOpen> message) {
        String id = message.content().commId();
        channels.busyThenIdle();
        channels.publish(new CustomCommClose(id, Transform.EMPTY_JSON_OBJ));
    }

    private void handleCommMsgCommand(Message<CustomCommMsg> ignored) {
        // no-op
    }

    private void handleCommCloseCommand(Message<CustomCommClose> ignored) {
        // no-op
    }

    private void handleCommInfoRequest(Message<ShellCommInfoRequest> message) {
        channels.busyThenIdle();
        channels.reply(new ShellCommInfoReply(new LinkedHashMap<>()));
    }
}
