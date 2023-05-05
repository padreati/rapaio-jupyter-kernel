package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.rapaio.jupyter.kernel.channels.ReplyEnv;
import org.rapaio.jupyter.kernel.core.ReplacementOptions;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalResult;
import org.rapaio.jupyter.kernel.core.magic.MagicEvaluator;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicParseException;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class LoadMagicHandler implements MagicHandler {

    private static final String PREFIX = "%load ";

    @Override
    public String name() {
        return "Load";
    }

    @Override
    public String syntax() {
        return "%load path_to_script_or_notebook";
    }

    @Override
    public List<String> helpMessage() {
        return List.of(
                "Loads and executes a local jshell script file or a jupyter notebook for java language."
        );
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet snippet) {
        return snippet.oneLine() && snippet.lines().size() == 1 && snippet.line(0).code().startsWith(PREFIX);
    }

    @Override
    public Object eval(MagicEvaluator magicEvaluator, JavaEngine engine, ReplyEnv env, MagicSnippet snippet) throws MagicParseException,
            MagicEvalException {
        if (!canHandleSnippet(snippet)) {
            throw new IllegalArgumentException("Magic handler cannot execute the given snippet.");
        }
        String allCode = snippet.line(0).code();
        String path = allCode.substring(PREFIX.length());

        File file = new File(path);

        if (file.isFile() && file.exists() && path.endsWith(".jshell")) {
            return evalShellScript(engine, env, file);
        }
        if (file.isFile() && file.exists() && path.endsWith(".ipynb")) {
            try {
                String content = Files.readString(Path.of(file.getAbsolutePath()));
                return evalNotebook(magicEvaluator, engine, env, snippet, content);
            } catch (IOException ex) {
                throw new MagicParseException("LoadMagicHandler",
                        snippet.line(0).code(),
                        "Cannot read file: '%s'".formatted(file.getAbsolutePath()));
            }
        }
        return null;
    }

    @Override
    public ReplacementOptions complete(ReplyEnv env, MagicSnippet snippet) {
        return HandlerUtils.oneLinePathComplete(PREFIX, snippet,
                f -> f.isDirectory() || f.getName().endsWith(".ipynb") || f.getName().endsWith(".jshell"));
    }

    private Object evalShellScript(JavaEngine engine, ReplyEnv env, File file) {
        env.writeToStdErr("Not implemented yet.");
        return null;
    }

    /**
     * For parsing notebook files follow the format specified in: <a href="http://ipython.org/ipython-doc/3/notebook/nbformat.html">nbformat</a>
     */
    Object evalNotebook(MagicEvaluator magicEvaluator, JavaEngine engine, ReplyEnv env, MagicSnippet snippet, String content) throws
            MagicEvalException {
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
                sb.append(codeLine.getAsString());
                // we should append a new line, it seems it is already contained
                // sb.append("\n")
            }
            String cellCode = sb.toString();
            try {
                MagicEvalResult magicResult = magicEvaluator.eval(env, cellCode);
                if (!magicResult.handled()) {
                    engine.eval(cellCode);
                }
            } catch (Exception e) {
                System.out.println("Error at:\n" + cellCode);
                throw new RuntimeException(e);
            }
        }
        return null;
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
}
