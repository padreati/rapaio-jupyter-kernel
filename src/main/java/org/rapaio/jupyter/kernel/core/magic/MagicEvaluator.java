package org.rapaio.jupyter.kernel.core.magic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.rapaio.jupyter.kernel.channels.ReplyEnv;
import org.rapaio.jupyter.kernel.core.ReplacementOptions;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.handlers.HelpMagicHandler;
import org.rapaio.jupyter.kernel.core.magic.handlers.JavaReplMagicHandler;

public class MagicEvaluator {

    private static final List<MagicHandler> magicHandlers = new LinkedList<>();

    static {
        // register magic handlers
        magicHandlers.add(new JavaReplMagicHandler());
        magicHandlers.add(new HelpMagicHandler(magicHandlers));
    }

    private final JavaEngine javaEngine;

    public MagicEvaluator(JavaEngine javaEngine) {
        this.javaEngine = javaEngine;
    }

    private record MagicPair(MagicSnippet snippet, MagicHandler handler) {
    }

    public MagicEvalResult eval(ReplyEnv env, String expr) throws MagicParseException {

        // parse magic snippets
        List<MagicSnippet> snippets = parseSnippets(expr, -1);
        if (snippets == null) {
            return new MagicEvalResult(false, null);
        }

        // find magic handlers for each snippet
        List<MagicPair> magicPairs = new ArrayList<>();
        for (var snippet : snippets) {
            boolean handled = false;
            for (var handler : magicHandlers) {
                // first handler do the job
                if (handler.canHandleSnippet(snippet)) {
                    magicPairs.add(new MagicPair(snippet, handler));
                    handled = true;
                    break;
                }
            }
            if (!handled) {
                return new MagicEvalResult(false, null);
            }
        }

        // if everything is fine then execute handlers
        Object lastResult = null;
        for (var pair : magicPairs) {
            lastResult = pair.handler.eval(env, pair.snippet);
        }

        // return last result
        return new MagicEvalResult(true, lastResult);
    }

    public MagicInspectResult inspect(ReplyEnv env, String expr, int cursorPosition) {

        // parse magic snippets
        List<MagicSnippet> snippets = parseSnippets(expr, cursorPosition);
        if (snippets == null) {
            return new MagicInspectResult(false, null);
        }

        // we have magic snippets, check if some of them contains position
        MagicSnippet snippet = null;
        for (var s : snippets) {
            if (s.lines().stream().anyMatch(MagicSnippet.CodeLine::hasPosition)) {
                snippet = s;
                break;
            }
        }

        // we have a magic code, but position is not found in an interesting snippet
        if (snippet == null) {
            return new MagicInspectResult(true, null);
        }

        // identify the code line which contains position
        MagicSnippet.CodeLine codeLine = snippet.lines().stream().filter(MagicSnippet.CodeLine::hasPosition).findAny().orElse(null);
        if (codeLine == null) {
            return new MagicInspectResult(true, null);
        }

        // ask handler to answer
        for (var handler : magicHandlers) {
            // first handler do the job
            if (handler.canHandleSnippet(snippet)) {
                DisplayData dd = handler.inspect(env, snippet);
                return new MagicInspectResult(true, dd);
            }
        }

        // TODO: we should not arrive here
        return new MagicInspectResult(true, null);
    }

    public MagicCompleteResult complete(ReplyEnv env, String expr, int cursorPosition) {

        // parse magic snippets
        List<MagicSnippet> snippets = parseSnippets(expr, cursorPosition);
        if (snippets == null) {
            return new MagicCompleteResult(false, null);
        }

        // we have magic snippets, check if some of them contains position
        MagicSnippet snippet = null;
        for (var s : snippets) {
            if (s.lines().stream().anyMatch(MagicSnippet.CodeLine::hasPosition)) {
                snippet = s;
                break;
            }
        }

        // we have a magic code, but position is not found in an interesting snippet
        if (snippet == null) {
            return new MagicCompleteResult(true, null);
        }

        // identify the code line which contains position
        MagicSnippet.CodeLine codeLine = snippet.lines().stream().filter(MagicSnippet.CodeLine::hasPosition).findAny().orElse(null);
        if (codeLine == null) {
            return new MagicCompleteResult(true, null);
        }

        // ask handler to answer
        for (var handler : magicHandlers) {
            // first handler do the job
            if (handler.canHandleSnippet(snippet)) {
                ReplacementOptions replacementOptions = handler.complete(env, snippet);
                return new MagicCompleteResult(true, replacementOptions);
            }
        }

        // TODO: we should not arrive here
        return new MagicCompleteResult(true, null);
    }

    private boolean hasLineMarker(String line) {
        if (line.length() < 2) {
            return false;
        }
        return line.charAt(0) == '%' && line.charAt(1) != '%' && !line.substring(1).trim().isEmpty();
    }

    private boolean hasCellMarker(String line) {
        if (line.length() < 2) {
            return false;
        }
        return line.charAt(0) == '%' && line.charAt(1) == '%' && !line.substring(2).trim().isEmpty();
    }

    private boolean hasComment(String line) {
        return line.startsWith("//");
    }

    private boolean isEmpty(String line) {
        return line.trim().isEmpty();
    }

    private List<MagicSnippet> parseSnippets(String sourceCode, int position) {
        String[] lines = sourceCode.split("\\R");

        List<MagicSnippet> snippets = new ArrayList<>();

        List<MagicSnippet.CodeLine> cellLines = null;

        int startLen = 0;
        for (String line : lines) {

            int endLen = startLen + line.length();
            boolean hasPosition = false;
            int relativePosition = -1;
            if (position >= startLen && position <= endLen) {
                hasPosition = true;
                relativePosition = position - startLen;
            }
            startLen += line.length() + 1;

            // skip empty lines and comments
            if (isEmpty(line) || hasComment(line)) {
                continue;
            }

            if (hasLineMarker(line)) {
                if (cellLines != null) {
                    snippets.add(new MagicSnippet(false, cellLines));
                    cellLines = null;
                }
                snippets.add(new MagicSnippet(true, List.of(new MagicSnippet.CodeLine(line, hasPosition, relativePosition, position))));
                continue;
            }

            if (hasCellMarker(line)) {
                cellLines = new ArrayList<>();
                cellLines.add(new MagicSnippet.CodeLine(line, hasPosition, relativePosition, position));
                continue;
            }

            if (cellLines == null) {
                return null;
            }

            cellLines.add(new MagicSnippet.CodeLine(line, hasPosition, relativePosition, position));
        }
        if (cellLines != null) {
            snippets.add(new MagicSnippet(false, cellLines));
        }
        return snippets;
    }

}
