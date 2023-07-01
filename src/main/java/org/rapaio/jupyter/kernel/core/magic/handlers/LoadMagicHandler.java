package org.rapaio.jupyter.kernel.core.magic.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.format.ErrorFormatters;
import org.rapaio.jupyter.kernel.core.magic.*;
import org.rapaio.jupyter.kernel.message.messages.IOPubError;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LoadMagicHandler extends MagicHandler {

    private static final String PREFIX = "%load";

    @Override
    public String name() {
        return "Load";
    }

    @Override
    public List<SnippetMagicHandler> snippetMagicHandlers() {
        return List.of(
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher("%load .*")
                        .syntaxHelp(List.of("%load path_to_script_or_notebook"))
                        .syntaxPrefix("%load ")
                        .documentation(List.of())
                        .canHandlePredicate(this::canHandleSnippet)
                        .evalFunction(this::evalLine)
                        .inspectFunction((channels, magicSnippet) -> null)
                        .completeFunction(this::completeLine)
                        .build()
        );
    }

    @Override
    public List<String> helpMessage() {
        return List.of(
                "Loads and executes a local jshell script file or a jupyter notebook for java language."
        );
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet magicSnippet) {
        return magicSnippet.isLineMagic() && magicSnippet.lines().size() == 1 && magicSnippet.line(0).code().startsWith(PREFIX);
    }

    Object evalLine(RapaioKernel kernel, MagicSnippet snippet) throws MagicEvalException {
        if (!canHandleSnippet(snippet)) {
            throw new IllegalArgumentException("Magic handler cannot execute the given snippet.");
        }
        String allCode = snippet.line(0).code();
        String path = allCode.substring(PREFIX.length() + 1).trim();

        File file = new File(path);

        if (file.isFile() && file.exists()) {
            try {
                String content = Files.readString(Path.of(file.getAbsolutePath()));
                if (path.endsWith(".ipynb")) {
                    return evalNotebook(kernel, snippet, content);
                } else {
                    return evalShellScript(kernel, content);
                }
            } catch (IOException ex) {
                throw new MagicEvalException(snippet, "Cannot read file: '%s'".formatted(file.getAbsolutePath()),
                        0, allCode.indexOf(PREFIX) + PREFIX.length(), allCode.length());
            }
        }
        return null;
    }

    CompleteMatches completeLine(RapaioKernel kernel, MagicSnippet snippet) {
        return MagicHandlerTools.oneLinePathComplete(PREFIX, snippet,
                f -> f.isDirectory() || f.getName().endsWith(".ipynb") || f.getName().endsWith(".jshell"));
    }

    private boolean checkLanguage(JsonElement root) {
        // since all metadata fields are optional, we do the check if we have information
        // otherwise we simply assume we deal with java and let the compiler fail

        if (root.isJsonObject() && root.getAsJsonObject().has("metadata")) {
            var metadata = root.getAsJsonObject().get("metadata").getAsJsonObject();
            if (metadata.has("language_info")) {
                var languageInfo = metadata.get("language_info").getAsJsonObject();
                if (languageInfo.has("name")) {
                    String languageName = languageInfo.get("name").getAsString();
                    return languageName.equalsIgnoreCase("java");
                }
            }
        }
        // no metadata, we think optimistic
        return true;
    }

    /**
     * For parsing notebook files follow the format specified in: <a href="http://ipython.org/ipython-doc/3/notebook/nbformat.html">nbformat</a>
     */
    Object evalNotebook(RapaioKernel kernel, MagicSnippet snippet, String content) throws MagicEvalException {
        JsonElement root = JsonParser.parseString(content);

        boolean checkLanguage = checkLanguage(root);
        if (!checkLanguage) {
            throw new MagicEvalException(snippet, "Cannot load a notebook which is not for Java language.");
        }

        var cells = root.getAsJsonObject().get("cells").getAsJsonArray();
        for (var cell : cells) {

            String cellType = cell.getAsJsonObject().get("cell_type").getAsString();
            if (!"code".equalsIgnoreCase(cellType)) {
                // skip cells which are not code
                continue;
            }
            if (!cell.getAsJsonObject().has("source")) {
                // should not happen, but if happens we can skip that
                continue;
            }

            var codeLines = cell.getAsJsonObject().get("source").getAsJsonArray();
            StringBuilder sb = new StringBuilder();
            for (var codeLine : codeLines) {
                String codeString = codeLine.getAsString();
                sb.append(codeString);
                // we should append a new line, it seems it is already contained
                if (!codeString.endsWith("\n")) {
                    sb.append("\n");
                }
            }
            String cellCode = sb.toString();
            try {
                MagicEvalResult magicResult = kernel.magicEngine().eval(cellCode);
                if (!magicResult.handled()) {
                    kernel.javaEngine().eval(cellCode);
                }
            } catch (Exception e) {
                kernel.channels().publish(IOPubError.of(e, ex -> ErrorFormatters.exceptionFormat(kernel, ex)));
                return null;
            }
        }
        return null;
    }

    Object evalShellScript(RapaioKernel kernel, String content) {
        try {
            return kernel.javaEngine().eval(content);
        } catch (Exception e) {
            kernel.channels().publish(IOPubError.of(e, ex -> ErrorFormatters.exceptionFormat(kernel, ex)));
            return null;
        }
    }
}
