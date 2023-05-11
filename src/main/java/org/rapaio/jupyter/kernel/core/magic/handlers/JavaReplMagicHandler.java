package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicParseException;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.OneLineMagicHandler;

import jdk.jshell.ImportSnippet;
import jdk.jshell.MethodSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.TypeDeclSnippet;
import jdk.jshell.VarSnippet;

public class JavaReplMagicHandler implements MagicHandler {

    public static final String LINE_PREFIX = "%jshell";

    @Override
    public String name() {
        return "JShell commands";
    }

    @Override
    public List<String> helpMessage() {
        return List.of("Magic handler which runs command against JShell REPL and displays the results.",
                "Not all JShell commands are implemented, since some of them does not make sense with "
                        + "notebooks (for example edit cell is handled simply by editing the corresponding "
                        + "code cell and run).");
    }

    @Override
    public List<OneLineMagicHandler> oneLineMagicHandlers() {
        return List.of(
                OneLineMagicHandler.builder()
                        .syntaxMatcher("%jshell /methods")
                        .syntaxHelp("%jshell /methods")
                        .documentation(List.of("List all active methods."))
                        .canHandlePredicate(magicSnippet -> canHandleSnippet(magicSnippet, "%jshell /methods"))
                        .evalFunction(this::evalMethods)
                        .completeFunction((channels, magicSnippet) -> null)
                        .build(),
                OneLineMagicHandler.builder()
                        .syntaxMatcher("%jshell /vars")
                        .syntaxHelp("%jshell /vars")
                        .documentation(List.of("List all active variables, with type and value."))
                        .canHandlePredicate(magicSnippet -> canHandleSnippet(magicSnippet, "%jshell /vars"))
                        .evalFunction(this::evalVars)
                        .completeFunction((channels, magicSnippet) -> null)
                        .build(),
                OneLineMagicHandler.builder()
                        .syntaxMatcher("%jshell /imports")
                        .syntaxHelp("%jshell /imports")
                        .documentation(List.of("List all active import statements."))
                        .canHandlePredicate(magicSnippet -> canHandleSnippet(magicSnippet, "%jshell /imports"))
                        .evalFunction(this::evalImports)
                        .completeFunction((channels, magicSnippet) -> null)
                        .build(),
                OneLineMagicHandler.builder()
                        .syntaxMatcher("%jshell /types")
                        .syntaxHelp("%jshell /types")
                        .documentation(List.of("List all active types: classes, interfaces, enums and annotations."))
                        .canHandlePredicate(magicSnippet -> canHandleSnippet(magicSnippet, "%jshell /types"))
                        .evalFunction(this::evalTypes)
                        .completeFunction((channels, magicSnippet) -> null)
                        .build(),

                OneLineMagicHandler.builder()
                        .syntaxMatcher("%jshell /list -all")
                        .syntaxHelp("%jshell /list -all")
                        .documentation(List.of("List all code snippets, either active, inactive or erroneous."))
                        .canHandlePredicate(magicSnippet -> canHandleSnippet(magicSnippet, "%jshell /list -all"))
                        .evalFunction((magicEngine, javaEngine, channels, snippet) -> evalAllList(javaEngine))
                        .completeFunction((channels, magicSnippet) -> null)
                        .build(),
                OneLineMagicHandler.builder()
                        .syntaxMatcher("%jshell /list \\w")
                        .syntaxHelp("%jshell /list [id]")
                        .documentation(List.of("List snippet with the given id."))
                        .canHandlePredicate(magicSnippet -> canHandleSnippet(magicSnippet, "%jshell /list "))
                        .evalFunction((magicEngine, javaEngine, channels, magicSnippet) -> evalIdList(javaEngine, magicSnippet))
                        .completeFunction((channels, magicSnippet) -> null)
                        .build(),
                OneLineMagicHandler.builder()
                        .syntaxMatcher("%jshell /list")
                        .syntaxHelp("%jshell /list")
                        .documentation(List.of("List all active code snippets."))
                        .canHandlePredicate(snippet -> canHandleSnippet(snippet, "%jshell /list"))
                        .evalFunction((magicEngine, javaEngine, channels, snippet) -> evalSimpleList(javaEngine))
                        .completeFunction((channels, magicSnippet) -> null)
                        .build()
        );
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet snippet) {
        if (snippet == null || !snippet.oneLine() || snippet.lines().size() != 1 || snippet.lines().get(0).code().trim().isEmpty()) {
            return false;
        }
        String expr = snippet.lines().get(0).code();
        List<String> tokens = Arrays.stream(expr.trim().split("\\s")).filter(s -> !s.isEmpty()).toList();
        if (tokens.isEmpty()) {
            return false;
        }
        return LINE_PREFIX.equals(tokens.get(0));
    }

    private boolean canHandleSnippet(MagicSnippet snippet, String prefix) {
        if (snippet == null || !snippet.oneLine() || snippet.lines().size() != 1 || snippet.lines().get(0).code().trim().isEmpty()) {
            return false;
        }
        String expr = snippet.lines().get(0).code();
        return expr.startsWith(prefix);
    }

    @Override
    public Object eval(MagicEngine magicEngine, JavaEngine javaEngine, Channels channels, MagicSnippet snippet) throws
            MagicEvalException, MagicParseException {
        if (!canHandleSnippet(snippet)) {
            throw new RuntimeException("Try to execute a magic snippet to improper handler.");
        }
        for (var oneLineMagic : oneLineMagicHandlers()) {
            if (oneLineMagic.canHandlePredicate().test(snippet)) {
                return oneLineMagic.evalFunction().eval(magicEngine, javaEngine, channels, snippet);
            }
        }
        channels.writeToStdErr("Command not executed either because there is no handler or due to a syntax error.");
        return null;
    }

    @Override
    public CompleteMatches complete(Channels channels, MagicSnippet snippet) {
        return null;
    }

    private Object evalSimpleList(JavaEngine javaEngine) {
        List<Snippet> snippets = javaEngine.getShell().snippets()
                .filter(s -> javaEngine.getShell().status(s).isActive())
                .toList();
        StringBuilder sb = new StringBuilder();
        for (Snippet snippet : snippets) {
            sb.append(ANSI.start().bold().text("id:").fgGreen().text(snippet.id()).reset().text(" ")
                    .bold().text("type:").fgGreen().text(snippet.kind().name()).text("\n")
                    .build());
            for (String line : ANSI.sourceCode(snippet.source())) {
                sb.append(line).append("\n");
            }
        }
        return DisplayData.withText(sb.toString());
    }

    private Object evalAllList(JavaEngine javaEngine) {
        List<Snippet> snippets = javaEngine.getShell().snippets().toList();
        StringBuilder sb = new StringBuilder();
        for (Snippet snippet : snippets) {
            sb.append(ANSI.start().bold().text("id:").fgGreen().text(snippet.id()).reset().text(" ")
                    .bold().text("type:").fgGreen().text(snippet.kind().name()).text("\n")
                    .build());
            for (String line : ANSI.sourceCode(snippet.source())) {
                sb.append(line).append("\n");
            }
        }
        return DisplayData.withText(sb.toString());
    }

    private Object evalIdList(JavaEngine javaEngine, MagicSnippet magicSnippet) throws
            MagicEvalException {
        String id = magicSnippet.line(0).code().trim().substring("%jshell /list ".length()).trim();
        Optional<Snippet> optional = javaEngine.getShell().snippets().filter(s -> s.id().equals(id.trim())).findAny();
        if (optional.isPresent()) {
            Snippet snippet = optional.get();
            StringBuilder sb = new StringBuilder();
            sb.append(ANSI.start().bold().text("id:").fgGreen().text(snippet.id()).reset().text(" ")
                    .bold().text("type:").fgGreen().text(snippet.kind().name()).text("\n")
                    .build());
            for (String line : ANSI.sourceCode(snippet.source())) {
                sb.append(line).append("\n");
            }
            return DisplayData.withText(sb.toString());
        }
        throw new MagicEvalException(magicSnippet, "No snippet with id: " + id + " was found.");
    }

    private Object evalMethods(MagicEngine magicEvaluator, JavaEngine javaEngine, Channels channels, MagicSnippet magicSnippet) throws
            MagicEvalException {
        String command = magicSnippet.line(0).code().trim().substring(JavaReplMagicHandler.LINE_PREFIX.length() + 1);
        String options = command.substring("/methods".length()).trim();

        if (!options.isEmpty()) {
            throw new MagicEvalException(magicSnippet, "%jshell /methods command does not have arguments.");
        }

        List<MethodSnippet> snippets = javaEngine.getShell().methods()
                .filter(s -> javaEngine.getShell().status(s).isActive())
                .toList();
        StringBuilder sb = new StringBuilder();
        for (MethodSnippet snippet : snippets) {
            String signature = snippet.signature();
            String[] tokens = signature.split("\\)");
            for (String sourceLine : ANSI.sourceCode(tokens[1].substring(0, tokens[1].length() - 1) + " " +
                    snippet.name() + "(" + snippet.parameterTypes() + ")")) {
                sb.append(sourceLine).append("\n");
            }
        }
        return DisplayData.withText(sb.toString());
    }

    private Object evalVars(MagicEngine magicEvaluator, JavaEngine javaEngine, Channels channels, MagicSnippet magicSnippet)
            throws MagicEvalException {
        String line = magicSnippet.line(0).code().trim();
        String command = line.substring(JavaReplMagicHandler.LINE_PREFIX.length() + 1);
        String options = command.substring("/vars".length()).trim();

        if (!options.isEmpty()) {
            throw new MagicEvalException(magicSnippet, "%jshell /vars command does not have arguments.");
        }

        List<VarSnippet> snippets = javaEngine.getShell().variables()
                .filter(s -> javaEngine.getShell().status(s).isActive())
                .toList();
        StringBuilder sb = new StringBuilder();
        for (VarSnippet snippet : snippets) {
            sb.append(ANSI.start().text("|    ").bold().text(snippet.typeName() + " ").fgGreen().text(snippet.name())
                    .reset().text(" = ")
                    .reset().text(javaEngine.getShell().varValue(snippet)).text("\n")
                    .build());
        }
        return DisplayData.withText(sb.toString());
    }

    private Object evalImports(MagicEngine magicEvaluator, JavaEngine javaEngine, Channels channels, MagicSnippet magicSnippet) throws
            MagicEvalException {
        String line = magicSnippet.line(0).code().trim();
        String command = line.substring(JavaReplMagicHandler.LINE_PREFIX.length() + 1);
        String options = command.substring("/imports".length()).trim();

        if (!options.isEmpty()) {
            throw new MagicEvalException(magicSnippet, "%jshell /imports command does not have arguments.");
        }

        List<ImportSnippet> snippets = javaEngine.getShell().imports()
                .filter(s -> javaEngine.getShell().status(s).isActive())
                .toList();
        StringBuilder sb = new StringBuilder();
        for (ImportSnippet snippet : snippets) {
            for(String sourceLine : ANSI.sourceCode((snippet.isStatic() ? "static " : "") + snippet.fullname())) {
                sb.append(sourceLine).append("\n");
            }
        }
        return DisplayData.withText(sb.toString());
    }

    private Object evalTypes(MagicEngine magicEvaluator, JavaEngine javaEngine, Channels channels, MagicSnippet magicSnippet) throws
            MagicEvalException {
        String line = magicSnippet.line(0).code().trim();
        String command = line.substring(JavaReplMagicHandler.LINE_PREFIX.length() + 1);
        String options = command.substring("/types".length()).trim();

        if (!options.isEmpty()) {
            throw new MagicEvalException(magicSnippet, "%jshell /types command does not have arguments.");
        }

        List<TypeDeclSnippet> snippets = javaEngine.getShell().types()
                .filter(s -> javaEngine.getShell().status(s).isActive())
                .toList();
        StringBuilder sb = new StringBuilder();
        for (TypeDeclSnippet snippet : snippets) {
            String type = switch (snippet.subKind()) {
                case CLASS_SUBKIND -> "class";
                case INTERFACE_SUBKIND -> "interface";
                case RECORD_SUBKIND -> "record";
                case ENUM_SUBKIND -> "enum";
                default -> "unknown";
            };
            List<String> sourceLines = ANSI.sourceCode(type + " " + snippet.name());
            for (String sourceLine : sourceLines) {
                sb.append(sourceLine).append("\n");
            }
        }
        return DisplayData.withText(sb.toString());
    }
}
