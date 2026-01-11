package org.rapaio.jupyter.kernel.core.magic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.ExecutionContext;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.display.DisplayData;
import org.rapaio.jupyter.kernel.core.magic.handlers.BashMagicHandler;
import org.rapaio.jupyter.kernel.core.magic.handlers.ClasspathMagicHandler;
import org.rapaio.jupyter.kernel.core.magic.handlers.DependencyHandler;
import org.rapaio.jupyter.kernel.core.magic.handlers.HelpMagicHandler;
import org.rapaio.jupyter.kernel.core.magic.handlers.ImageMagicHandler;
import org.rapaio.jupyter.kernel.core.magic.handlers.JarMagicHandler;
import org.rapaio.jupyter.kernel.core.magic.handlers.JavaReplMagicHandler;
import org.rapaio.jupyter.kernel.core.magic.handlers.LoadMagicHandler;
import org.rapaio.jupyter.kernel.core.magic.interpolate.StringInterpolator;

public class MagicEngine {

    private static final List<MagicHandler> magicHandlers = new LinkedList<>();

    static {
        // register magic handlers
        magicHandlers.add(new JavaReplMagicHandler());
        magicHandlers.add(new DependencyHandler());
        magicHandlers.add(new JarMagicHandler());
        magicHandlers.add(new LoadMagicHandler());
        magicHandlers.add(new ImageMagicHandler());
        magicHandlers.add(new ClasspathMagicHandler());
        magicHandlers.add(new BashMagicHandler());
        magicHandlers.add(new HelpMagicHandler(magicHandlers));
    }

    private final MagicParser parser = new MagicParser();
    private final StringInterpolator interpolator = new StringInterpolator();
    private final RapaioKernel kernel;

    public MagicEngine(RapaioKernel kernel) {
        this.kernel = kernel;
    }

    private record MagicPair(MagicSnippet snippet, MagicHandler handler) {
    }

    public MagicEvalResult eval(String expr) throws MagicParseException, MagicEvalException {
        return eval(kernel.executionContext(), expr);
    }

    public MagicEvalResult eval(ExecutionContext context, String expr) throws MagicParseException, MagicEvalException {

        // parse magic snippets
        List<MagicSnippet> snippets = parser.parseSnippets(expr, -1);
        if (snippets == null) {
            return new MagicEvalResult(false, null);
        }

        if (parser.containsMixedSnippets(snippets)) {
            throw new MagicParseException("Mixed magic and Java code snippets are not allowed.");
        }

        if (!parser.canHandleByMagic(snippets)) {
            return new MagicEvalResult(false, null);
        }

        try {
            snippets = interpolateSnippets(context, snippets);
        } catch (Exception ex) {
            throw new MagicParseException("Magic interpolation failed: " + ex.getMessage());
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
            lastResult = pair.handler.eval(kernel, pair.snippet);
        }

        // return last result
        return new MagicEvalResult(true, lastResult);
    }

    public List<MagicSnippet> interpolateSnippets(ExecutionContext context, List<MagicSnippet> snippets) throws Exception {
        List<MagicSnippet> interpolatedSnippets = new ArrayList<>();
        for (MagicSnippet snippet : snippets) {
            if (snippet.type() == MagicSnippet.Type.NON_MAGIC) {
                // interpolation for magic snippets only
                interpolatedSnippets.add(snippet);
            }
            List<MagicSnippet.CodeLine> interpolatedLines = new ArrayList<>();
            for (MagicSnippet.CodeLine codeLine : snippet.lines()) {
                interpolatedLines.add(new MagicSnippet.CodeLine(interpolator.interpolate(context, kernel, codeLine.code()),
                        codeLine.hasPosition(), codeLine.relativePosition(), codeLine.globalPosition()));
            }

            interpolatedSnippets.add(new MagicSnippet(snippet.type(), interpolatedLines));
        }
        return interpolatedSnippets;
    }

    public MagicInspectResult inspect(ExecutionContext context, String expr, int cursorPosition) throws MagicEvalException,
            MagicParseException {

        // parse magic snippets
        List<MagicSnippet> snippets = parser.parseSnippets(expr, cursorPosition);
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

        if (snippet.type() == MagicSnippet.Type.NON_MAGIC) {
            return new MagicInspectResult(false, null);
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
                DisplayData dd = handler.inspect(kernel, snippet);
                return new MagicInspectResult(true, dd);
            }
        }

        // we should not arrive here
        return new MagicInspectResult(true, null);
    }

    public MagicCompleteResult complete(RapaioKernel kernel, String expr, int cursorPosition) throws
            MagicEvalException,
            MagicParseException {

        // parse magic snippets
        List<MagicSnippet> snippets = parser.parseSnippets(expr, cursorPosition);
        if (snippets == null) {
            return new MagicCompleteResult(false, null);
        }
        if (parser.containsMixedSnippets(snippets)) {
            // TOD: maybe better
            return new MagicCompleteResult(true, null);
        }
        if (!parser.canHandleByMagic(snippets)) {
            return new MagicCompleteResult(false, null);
        }

        // we have magic snippets, check if some of them contains position
        Optional<MagicSnippet> optionalMagicSnippet =
                snippets.stream().filter(s -> s.lines().stream().anyMatch(MagicSnippet.CodeLine::hasPosition))
                        .findFirst();

        // valid magic code, but position is not found in an interesting snippet
        if (optionalMagicSnippet.isEmpty()) {
            return new MagicCompleteResult(true, null);
        }

        // identify the code line which contains position
        MagicSnippet snippet = optionalMagicSnippet.get();
        MagicSnippet.CodeLine codeLine = snippet.lines().stream().filter(MagicSnippet.CodeLine::hasPosition).findAny().orElse(null);
        if (codeLine == null) {
            return new MagicCompleteResult(true, null);
        }

        // ask handler to answer
        for (var handler : magicHandlers) {
            // first handler do the job
            if (handler.canHandleSnippet(snippet)) {
                boolean handled = false;
                for (var snippetMagicHandler : handler.snippetMagicHandlers()) {
                    if (snippetMagicHandler.canHandlePredicate().test(snippet)) {
                        CompleteMatches matches = snippetMagicHandler.completeFunction().apply(kernel, snippet);
                        if (matches == null) {
                            handled = true;
                            break;
                        }
                        return new MagicCompleteResult(true, matches);
                    }
                }
                if (handled) {
                    break;
                }
            }
        }

        // if none give a specific answer, then use prefixes
        List<String> prefixes = new ArrayList<>();
        String line = codeLine.code();
        String linePrefix = line.substring(0, codeLine.relativePosition());
        for (var handler : magicHandlers) {
            for (var oneLineHandler : handler.snippetMagicHandlers()) {
                String prefix = oneLineHandler.syntaxPrefix();
                if (prefix.startsWith(linePrefix) && !prefix.equals(linePrefix)) {
                    prefixes.add(prefix);
                }
            }
        }
        prefixes.sort(String::compareTo);
        CompleteMatches matches = new CompleteMatches(prefixes, codeLine.globalPosition() - codeLine.relativePosition(),
                codeLine.globalPosition() + codeLine.code().length() - codeLine.relativePosition());
        return new MagicCompleteResult(true, matches);
    }

    public MagicIsCompleteResult isComplete(RapaioKernel kernel, String code) {
        // TODO: implement iscomplete logic here
        return MagicIsCompleteResult.notHandled();
    }
}
