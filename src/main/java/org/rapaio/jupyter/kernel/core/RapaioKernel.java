package org.rapaio.jupyter.kernel.core;

import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.Renderer;
import org.rapaio.jupyter.kernel.core.format.ErrorFormatters;
import org.rapaio.jupyter.kernel.core.java.IsCompleteResult;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.java.io.JShellConsole;
import org.rapaio.jupyter.kernel.core.magic.*;
import org.rapaio.jupyter.kernel.message.Header;
import org.rapaio.jupyter.kernel.message.Message;
import org.rapaio.jupyter.kernel.message.MessageType;
import org.rapaio.jupyter.kernel.message.messages.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

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
        this.magicEngine = new MagicEngine(this);
        this.renderer = new Renderer();
    }


    public Channels channels() {
        return channels;
    }

    public JavaEngine javaEngine() {
        return javaEngine;
    }

    public MagicEngine magicEngine() {
        return magicEngine;
    }

    public Renderer renderer() {
        return renderer;
    }

    public void display(DisplayData dd) {
        if (channels.hasMsgId()) {
            channels.publish(new IOPubDisplayData(dd));
        }
    }

    public void updateDisplay(String id, DisplayData dd) {
        dd.setDisplayId(id);
        updateDisplay(dd);
    }

    public void updateDisplay(DisplayData dd) {
        if (channels.hasMsgId()) {
            if (!dd.hasDisplayId()) {
                throw new IllegalArgumentException("Cannot update a display without display_id.");
            }
            channels.publish(new IOPubUpdateDisplayData(dd));
        }
    }

    private ShellKernelInfoReply kernelInfo() {

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
        return magicEngine.eval(expr);
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

    public void registerChannels(Channels channels) {
        this.channels = channels;
    }

    public void handleExecuteRequest(Message<ShellExecuteRequest> message) {
        ShellExecuteRequest request = message.content();

        int count = executionCount.getAndIncrement();

        channels.scheduleAfter(channels::freeMsgId);
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
                channels.scheduleAfter(() -> channels.reply(ShellExecuteReply.withOk(count, Collections.emptyMap())));
                return;
            }
        } catch (Exception e) {
            channels.publish(IOPubError.of(e, ex -> ErrorFormatters.exceptionFormat(this, ex)));
            channels.scheduleAfter(() -> channels.replyError(MessageType.SHELL_EXECUTE_REPLY.newError(), ErrorReply.of(this, e, count)));
            return;
        }

        // collect old streams
        PrintStream oldStdOut = System.out;
        PrintStream oldStdErr = System.err;
        InputStream oldStdIn = System.in;

        // some clients don't allow asking input
        boolean allowStdin = message.content().stdinEnabled();
        // hook system io to allow execution to output into notebook
        System.setIn(shellConsole.getIn());
        System.setOut(new PrintStream(shellConsole.getOut(), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(shellConsole.getErr(), true, StandardCharsets.UTF_8));

        shellConsole.bindChannels(channels, allowStdin);

        // push on stack restore streams
        channels.scheduleAfter(() -> {
            System.setOut(oldStdOut);
            System.setErr(oldStdErr);
            System.setIn(oldStdIn);
        });

        // unbind environment
        channels.scheduleAfter(shellConsole::unbindChannels);
        // flush before restoring
        channels.scheduleAfter(shellConsole::flush);

        try {
            DisplayData out = eval(request.code());
            if (out != null) {
                channels.publish(new IOPubExecuteResult(count, out));
            }
            channels.scheduleAfter(() -> channels.reply(ShellExecuteReply.withOk(count, Collections.emptyMap())));
        } catch (Exception e) {
            channels.publish(IOPubError.of(e, ex -> ErrorFormatters.exceptionFormat(this, ex)));
            channels.scheduleAfter(() -> channels.replyError(MessageType.SHELL_EXECUTE_REPLY.newError(), ErrorReply.of(this, e, count)));
        }
    }

    public void handleIsCompleteRequest(Message<ShellIsCompleteRequest> message) {
        ShellIsCompleteRequest request = message.content();
        channels.busyThenIdle();

        MagicIsCompleteResult magicResult = magicEngine.isComplete(this, request.code());
        if (magicResult.handled()) {
            channels.reply(magicResult.buildReply());
            return;
        }

        IsCompleteResult result = javaEngine.isComplete(request.code());
        channels.reply(result.buildReply());
    }

    public void handleInspectRequest(Message<ShellInspectRequest> message) {
        ShellInspectRequest request = message.content();
        channels.busyThenIdle();
        try {
            MagicInspectResult magicResult = magicEngine.inspect(request.code(), request.cursorPos());
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
            channels.replyError(MessageType.SHELL_INSPECT_REPLY.newError(), ErrorReply.of(this, e, 0));
        }
    }

    public void handleCompleteRequest(Message<ShellCompleteRequest> message) {
        ShellCompleteRequest request = message.content();
        channels.busyThenIdle();
        try {
            MagicCompleteResult magicResult = magicEngine.complete(this, request.code(), request.cursorPos());

            CompleteMatches options = magicResult.handled()
                    ? magicResult.replacementOptions()
                    : javaEngine.complete(request.code(), request.cursorPos());

            if (options == null) {
                channels.reply(ShellCompleteReply.empty(request.cursorPos()));
            } else {
                channels.reply(ShellCompleteReply.from(options));
            }
        } catch (Exception e) {
            channels.replyError(MessageType.SHELL_COMPLETE_REPLY.newError(), ErrorReply.of(this, e, 0));
        }
    }

    public void handleHistoryRequest(Message<ShellHistoryRequest> message) {
        /*
        Note from specifications:
        Most of the history messaging options are not used by Jupyter frontends, and many kernels do not implement them.
        If you’re implementing these messages in a kernel, the ‘tail’ request is the most useful;
        this is used by the Qt console, for example.
        The notebook interface does not use history messages at all.
         */
        LOGGER.info("ShellHistoryRequest not implemented.");
    }

    public void handleKernelInfoRequest(Message<ShellKernelInfoRequest> ignored) {
        channels.busyThenIdle();
        channels.reply(kernelInfo());
    }

    public void handleShutdownRequest(Message<ControlShutdownRequest> message) {
        ControlShutdownRequest request = message.content();
        channels.busyThenIdle();

        channels.scheduleAfter(() -> channels.reply(request.restart() ? ControlShutdownReply.SHUTDOWN_AND_RESTART : ControlShutdownReply.SHUTDOWN));

        // request.restart() is no use for now, but might be used in the end
        onShutdown();
        channels.runAfterActions();
        // this will determine the connections to shut down
        channels.markForShutdown();
    }

    public void handleInterruptRequest(Message<ControlInterruptRequest> message) {
        channels.busyThenIdle();
        channels.reply(new ControlInterruptReply());
        interrupt();
    }

    // custom communication is not implemented, however, we have to behave properly

    public void handleCommOpenCommand(Message<CustomCommOpen> message) {
        String id = message.content().commId();
        channels.busyThenIdle();
        channels.publish(new CustomCommClose(id, Transform.EMPTY_JSON_OBJ));
    }

    public void handleCommMsgCommand(Message<CustomCommMsg> ignored) {
        // no-op
    }

    public void handleCommCloseCommand(Message<CustomCommClose> ignored) {
        // no-op
    }

    public void handleCommInfoRequest(Message<ShellCommInfoRequest> message) {
        channels.busyThenIdle();
        channels.reply(new ShellCommInfoReply(new LinkedHashMap<>()));
    }
}
