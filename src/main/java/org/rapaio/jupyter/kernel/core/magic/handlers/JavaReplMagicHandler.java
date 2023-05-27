package org.rapaio.jupyter.kernel.core.magic.handlers;

import jdk.jshell.*;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.DisplayData;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.magic.MagicEvalException;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.SnippetMagicHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JavaReplMagicHandler extends MagicHandler {

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
    public List<SnippetMagicHandler> snippetMagicHandlers() {
        return List.of(
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher("%jshell /methods")
                        .syntaxHelp(List.of("%jshell /methods"))
                        .syntaxPrefix("%jshell /methods")
                        .documentation(List.of("List all active methods."))
                        .canHandlePredicate(magicSnippet -> canHandleSnippet(magicSnippet, "%jshell /methods"))
                        .evalFunction(this::evalMethods)
                        .completeFunction((kernel, magicSnippet) -> null)
                        .build(),
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher("%jshell /vars")
                        .syntaxHelp(List.of("%jshell /vars"))
                        .syntaxPrefix("%jshell /vars")
                        .documentation(List.of("List all active variables, with type and value."))
                        .canHandlePredicate(magicSnippet -> canHandleSnippet(magicSnippet, "%jshell /vars"))
                        .evalFunction(this::evalVars)
                        .completeFunction((kernel, magicSnippet) -> null)
                        .build(),
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher("%jshell /imports")
                        .syntaxHelp(List.of("%jshell /imports"))
                        .syntaxPrefix("%jshell /imports")
                        .documentation(List.of("List all active import statements."))
                        .canHandlePredicate(magicSnippet -> canHandleSnippet(magicSnippet, "%jshell /imports"))
                        .evalFunction(this::evalImports)
                        .completeFunction((kernel, magicSnippet) -> null)
                        .build(),
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher("%jshell /types")
                        .syntaxHelp(List.of("%jshell /types"))
                        .syntaxPrefix("%jshell /types")
                        .documentation(List.of("List all active types: classes, interfaces, enums and annotations."))
                        .canHandlePredicate(magicSnippet -> canHandleSnippet(magicSnippet, "%jshell /types"))
                        .evalFunction(this::evalTypes)
                        .completeFunction((kernel, magicSnippet) -> null)
                        .build(),

                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher("%jshell /list -all")
                        .syntaxHelp(List.of("%jshell /list -all"))
                        .syntaxPrefix("%jshell /list -all")
                        .documentation(List.of("List all code snippets, either active, inactive or erroneous."))
                        .canHandlePredicate(magicSnippet -> canHandleSnippet(magicSnippet, "%jshell /list -all"))
                        .evalFunction(this::evalAllList)
                        .completeFunction((kernel, magicSnippet) -> null)
                        .build(),
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher("%jshell /list \\w")
                        .syntaxHelp(List.of("%jshell /list [id]"))
                        .syntaxPrefix("%jshell /list ")
                        .documentation(List.of("List snippet with the given id."))
                        .canHandlePredicate(magicSnippet -> canHandleSnippet(magicSnippet, "%jshell /list "))
                        .evalFunction(this::evalIdList)
                        .completeFunction((kernel, magicSnippet) -> null)
                        .build(),
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher("%jshell /list")
                        .syntaxHelp(List.of("%jshell /list"))
                        .syntaxPrefix("%jshell /list")
                        .documentation(List.of("List all active code snippets."))
                        .canHandlePredicate(snippet -> canHandleSnippet(snippet, "%jshell /list"))
                        .evalFunction(this::evalSimpleList)
                        .completeFunction((kernel, magicSnippet) -> null)
                        .build()
        );
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet magicSnippet) {
        if (magicSnippet == null || !magicSnippet.isLineMagic() || magicSnippet.lines().size() != 1 ||
                magicSnippet.lines().get(0).code().trim().isEmpty()) {
            return false;
        }
        String expr = magicSnippet.lines().get(0).code();
        List<String> tokens = Arrays.stream(expr.trim().split("\\s")).filter(s -> !s.isEmpty()).toList();
        if (tokens.isEmpty()) {
            return false;
        }
        return LINE_PREFIX.equals(tokens.get(0));
    }

    private boolean canHandleSnippet(MagicSnippet snippet, String prefix) {
        if (snippet == null || !snippet.isLineMagic() || snippet.lines().size() != 1 || snippet.lines().get(0).code().trim().isEmpty()) {
            return false;
        }
        String expr = snippet.lines().get(0).code();
        return expr.startsWith(prefix);
    }

    private Object evalSimpleList(RapaioKernel kernel, MagicSnippet magicSnippet) {
        List<Snippet> snippets = kernel.javaEngine().getShell().snippets()
                .filter(s -> kernel.javaEngine().getShell().status(s).isActive())
                .toList();
        StringBuilder sb = new StringBuilder();
        for (Snippet snippet : snippets) {
            sb.append(ANSI.start().bold().text("id:").fgGreen().text(snippet.id()).reset().text(" ")
                    .bold().text("type:").fgGreen().text(snippet.kind().name()).text("\n")
                    .render());
            for (String line : ANSI.sourceCode(snippet.source())) {
                sb.append(line).append("\n");
            }
        }
        return DisplayData.withText(sb.toString());
    }

    private Object evalAllList(RapaioKernel kernel, MagicSnippet magicSnippet) {
        List<Snippet> snippets = kernel.javaEngine().getShell().snippets().toList();
        StringBuilder sb = new StringBuilder();
        for (Snippet snippet : snippets) {
            sb.append(ANSI.start().bold().text("id:").fgGreen().text(snippet.id()).reset().text(" ")
                    .bold().text("type:").fgGreen().text(snippet.kind().name()).text("\n")
                    .render());
            for (String line : ANSI.sourceCode(snippet.source())) {
                sb.append(line).append("\n");
            }
        }
        return DisplayData.withText(sb.toString());
    }

    private Object evalIdList(RapaioKernel kernel, MagicSnippet magicSnippet) throws
            MagicEvalException {
        String id = magicSnippet.line(0).code().trim().substring("%jshell /list ".length()).trim();
        Optional<Snippet> optional = kernel.javaEngine().getShell().snippets().filter(s -> s.id().equals(id.trim())).findAny();
        if (optional.isPresent()) {
            Snippet snippet = optional.get();
            StringBuilder sb = new StringBuilder();
            sb.append(ANSI.start().bold().text("id:").fgGreen().text(snippet.id()).reset().text(" ")
                    .bold().text("type:").fgGreen().text(snippet.kind().name()).text("\n")
                    .render());
            for (String line : ANSI.sourceCode(snippet.source())) {
                sb.append(line).append("\n");
            }
            return DisplayData.withText(sb.toString());
        }
        throw new MagicEvalException(magicSnippet, "No snippet with id: " + id + " was found.");
    }

    private Object evalMethods(RapaioKernel kernel, MagicSnippet magicSnippet) throws
            MagicEvalException {
        String command = magicSnippet.line(0).code().trim().substring(JavaReplMagicHandler.LINE_PREFIX.length() + 1);
        String options = command.substring("/methods".length()).trim();

        if (!options.isEmpty()) {
            throw new MagicEvalException(magicSnippet, "%jshell /methods command does not have arguments.");
        }

        List<MethodSnippet> snippets = kernel.javaEngine().getShell().methods()
                .filter(s -> kernel.javaEngine().getShell().status(s).isActive())
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

    private Object evalVars(RapaioKernel kernel, MagicSnippet magicSnippet) throws MagicEvalException {
        String line = magicSnippet.line(0).code().trim();
        String command = line.substring(JavaReplMagicHandler.LINE_PREFIX.length() + 1);
        String options = command.substring("/vars".length()).trim();

        if (!options.isEmpty()) {
            throw new MagicEvalException(magicSnippet, "%jshell /vars command does not have arguments.");
        }

        List<VarSnippet> snippets = kernel.javaEngine().getShell().variables()
                .filter(s -> kernel.javaEngine().getShell().status(s).isActive())
                .toList();
        StringBuilder sb = new StringBuilder();
        for (VarSnippet snippet : snippets) {
            sb.append(ANSI.start().text("|    ").bold().text(snippet.typeName() + " ").fgGreen().text(snippet.name())
                    .reset().text(" = ")
                    .reset().text(kernel.javaEngine().getShell().varValue(snippet)).text("\n")
                    .render());
        }
        return DisplayData.withText(sb.toString());
    }

    private Object evalImports(RapaioKernel kernel, MagicSnippet magicSnippet) throws
            MagicEvalException {
        String line = magicSnippet.line(0).code().trim();
        String command = line.substring(JavaReplMagicHandler.LINE_PREFIX.length() + 1);
        String options = command.substring("/imports".length()).trim();

        if (!options.isEmpty()) {
            throw new MagicEvalException(magicSnippet, "%jshell /imports command does not have arguments.");
        }

        List<ImportSnippet> snippets = kernel.javaEngine().getShell().imports()
                .filter(s -> kernel.javaEngine().getShell().status(s).isActive())
                .toList();
        StringBuilder sb = new StringBuilder();
        for (ImportSnippet snippet : snippets) {
            for (String sourceLine : ANSI.sourceCode((snippet.isStatic() ? "static " : "") + snippet.fullname())) {
                sb.append(sourceLine).append("\n");
            }
        }
        return DisplayData.withText(sb.toString());
    }

    private Object evalTypes(RapaioKernel kernel, MagicSnippet magicSnippet) throws MagicEvalException {
        String line = magicSnippet.line(0).code().trim();
        String command = line.substring(JavaReplMagicHandler.LINE_PREFIX.length() + 1);
        String options = command.substring("/types".length()).trim();

        if (!options.isEmpty()) {
            throw new MagicEvalException(magicSnippet, "%jshell /types command does not have arguments.");
        }

        List<TypeDeclSnippet> snippets = kernel.javaEngine().getShell().types()
                .filter(s -> kernel.javaEngine().getShell().status(s).isActive())
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
