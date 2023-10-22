package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.apache.ivy.core.report.ResolveReport;
import org.rapaio.jupyter.kernel.core.ExecutionContext;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.SnippetMagicHandler;
import org.rapaio.jupyter.kernel.core.magic.dependencies.Dependency;

public class DependencyHandler extends MagicHandler {

    private static final String HEADER = "%dependency";

    @Override
    public String name() {
        return "Dependency manager";
    }

    @Override
    public List<SnippetMagicHandler> snippetMagicHandlers() {
        return List.of(

                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher(HEADER + " /list-configuration")
                        .syntaxHelp(List.of(HEADER + " /list-configuration"))
                        .syntaxPrefix(HEADER + " /list-configuration")
                        .documentation(List.of(
                                "List all the dependency configurations"
                        ))
                        .canHandlePredicate(snippet -> canHandleOneLinePrefix(snippet, HEADER + " /list-configuration"))
                        .evalFunction(this::evalLineListConfiguration)
                        .build(),
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher(HEADER + " /list-artifacts")
                        .syntaxHelp(List.of(HEADER + " /list-artifacts"))
                        .syntaxPrefix(HEADER + " /list-artifacts")
                        .documentation(List.of(
                                "List artifacts loaded after the last dependency resolve"
                        ))
                        .canHandlePredicate(snippet -> canHandleOneLinePrefix(snippet, HEADER + " /list-artifacts"))
                        .evalFunction(this::evalLineListArtifacts)
                        .build(),
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher(HEADER + " /conflict-manager [a-z]+")
                        .syntaxHelp(List.of(
                                HEADER + " /conflict-manager all|latest-time|latest-revision(default)|latest-compatible|strict"
                        ))
                        .syntaxPrefix(HEADER + " /conflict-manager ")
                        .documentation(List.of(
                                "Configures a conflict manager. A conflict manager describes how conflicts are resolved.",
                                "all: resolve conflicts by selecting all revisions, it doesn’t evict any modules.",
                                "latest-time: selects only the latest in time revision.",
                                "latest-revision: selects only the latest revision (default value).",
                                "latest-compatible: selects the latest version in the conflicts which can result in a "
                                        + "compatible set of dependencies. This conflict manager does not allow any conflicts "
                                        + "(similar to the strict conflict manager), except that it follows a best effort strategy to "
                                        + "try to find a set of compatible modules (according to the version constraints)",
                                "strict: throws an exception (i.e. causes a build failure) whenever a conflict is found. It does not "
                                        + "take into consideration overrides."
                        ))
                        .canHandlePredicate(snippet -> canHandleOneLinePrefix(snippet, HEADER + " /conflict-manager"))
                        .evalFunction(this::evalLineConflictManager)
                        .build(),
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher(HEADER + " /resolve")
                        .syntaxHelp(List.of(HEADER + " /resolve"))
                        .syntaxPrefix(HEADER + " /resolve")
                        .documentation(List.of("Resolve dependencies"))
                        .canHandlePredicate(snippet -> canHandleOneLinePrefix(snippet, HEADER + " /resolve"))
                        .evalFunction(this::evalLineResolve)
                        .build(),
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher(HEADER + " /add( \\S+)+ (\\-\\-force)*")
                        .syntaxHelp(List.of(
                                HEADER + " /add group_id:artifact_id:version",
                                HEADER + " /add group_id:artifact_id:version --force"
                        ))
                        .syntaxPrefix(HEADER + " /add")
                        .documentation(List.of(
                                "Declares a direct dependency to dependency manager.",
                                "Flag /force can be used in order to force version overrides.",
                                "This command does not resolve dependencies. "
                        ))
                        .canHandlePredicate(snippet -> canHandleOneLinePrefix(snippet, HEADER + " /add "))
                        .evalFunction(this::evalLineAdd)
                        .build(),
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher(HEADER + " /override( \\S+)+")
                        .syntaxHelp(List.of(
                                HEADER + " /override group_id:artifact_id:version"
                        ))
                        .syntaxPrefix(HEADER + " /override")
                        .documentation(List.of(
                                "Declares an override, dependencies matched by group_id and artifact_id "
                                        + "will be replaced with this override.",
                                "A more flexible way to solve conflicts, even if a conflict actually does not exist.",
                                "It cannot be used to override forced direct dependencies."
                        ))
                        .canHandlePredicate(snippet -> canHandleOneLinePrefix(snippet, HEADER + " /override "))
                        .evalFunction(this::evalLineOverride)
                        .build()
        );
    }

    @Override
    public List<String> helpMessage() {
        return List.of("Find and resolve a dependency using coordinates: group_id, artifact_id and version id.",
                "The maven public repositories are searched for dependencies. Additionally, any maven transitive dependency declared with "
                        + "magic handlers are included in classpath.");
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet magicSnippet) {
        if (!magicSnippet.isLineMagic()) {
            return false;
        }
        return magicSnippet.line(0).code().startsWith(HEADER);
    }

    Object evalLineConflictManager(RapaioKernel kernel, ExecutionContext context, MagicSnippet snippet) {
        if (!canHandleOneLinePrefix(snippet, HEADER + " /conflict-manager")) {
            throw new RuntimeException("Cannot evaluate the given magic snippet.");
        }
        String fullCode = snippet.line(0).code();
        String args = fullCode.substring((HEADER + " /conflict-manager").length()).trim();
        switch (args) {
            case "all":
                kernel.dependencyManager().setAllConflictManager();
                break;
            case "latest-time":
                kernel.dependencyManager().setLatestTimeConflictManager();
                break;
            case "latest-revision":
                kernel.dependencyManager().setLatestRevisionConflictManager();
                break;
            case "latest-compatible":
                kernel.dependencyManager().setLatestCompatibleConflictManager();
                break;
            case "strict":
                kernel.dependencyManager().setStrictConflictManager();
                break;
            default:
                throw new RuntimeException("Conflict manager not recognized.");
        }
        return null;
    }

    Object evalLineAdd(RapaioKernel kernel, ExecutionContext context, MagicSnippet snippet) {
        if (!canHandleOneLinePrefix(snippet, HEADER + " /add ")) {
            throw new RuntimeException("Cannot evaluate the given magic snippet.");
        }
        String fullCode = snippet.line(0).code();
        String[] args = fullCode.substring((HEADER + " /add ").length()).trim().split(" ");

        if (args.length == 2 && !args[1].equals("--force")) {
            throw new RuntimeException("Error parsing '" + fullCode + "'");
        }

        kernel.channels().writeToStdOut("Adding dependency " + ANSI.start().bold().fgGreen().text(args[0]).reset().render() + "\n");
        kernel.dependencyManager().addDependency(new Dependency(args[0], args.length == 2));
        return null;
    }

    Object evalLineOverride(RapaioKernel kernel, ExecutionContext context, MagicSnippet snippet) {
        if (!canHandleOneLinePrefix(snippet, HEADER + " /override ")) {
            throw new RuntimeException("Cannot evaluate the given magic snippet.");
        }
        String fullCode = snippet.line(0).code();
        String args = fullCode.substring((HEADER + " /override ").length()).trim();

        kernel.channels().writeToStdOut("Adding dependency override " + ANSI.start().bold().fgGreen().text(args).reset().render() + "\n");
        kernel.dependencyManager().addOverrideDependency(new Dependency(args, true));
        return null;
    }

    Object evalLineResolve(RapaioKernel kernel, ExecutionContext context, MagicSnippet snippet) {
        if (!canHandleOneLinePrefix(snippet, HEADER + " /resolve")) {
            throw new RuntimeException("Cannot evaluate the given magic snippet.");
        }

        try {
            kernel.channels().writeToStdOut("Solving dependencies/n");
            ResolveReport resolveReport = kernel.dependencyManager().resolve();
            var problemMessages = resolveReport.getAllProblemMessages();
            for (var problemMessage : problemMessages) {
                kernel.channels().writeToStdErr(problemMessage);
            }

            var artifactsReports = resolveReport.getAllArtifactsReports();
            kernel.channels().writeToStdOut("Found dependencies count: " + artifactsReports.length + "\n");
            for (var report : artifactsReports) {
                kernel.channels().writeToStdOut("Add to classpath: " +
                        ANSI.start().fgGreen().text(report.getLocalFile().getAbsolutePath()).reset().render() + "\n");
                kernel.javaEngine().getShell().addToClasspath(report.getLocalFile().getAbsolutePath());
                kernel.dependencyManager().addLoadedArtifact(report);
            }
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    Object evalLineListConfiguration(RapaioKernel kernel, ExecutionContext context, MagicSnippet snippet) {
        if (!canHandleOneLinePrefix(snippet, HEADER + " /list-configuration")) {
            throw new RuntimeException("Cannot evaluate the given magic snippet.");
        }
        var channels = kernel.channels();
        var dm = kernel.dependencyManager();
        channels.writeToStdOut("Direct dependencies count: " + dm.getDirectDependencies().size() + "\n");
        for (var dep : dm.getDirectDependencies()) {
            channels.writeToStdOut(ANSI.start().text(" - ").bold().fgGreen().text(dep.revisionId().toString()).nl().render());
        }
        channels.writeToStdOut("Dependency overrides count: " + dm.getConflictDependencies().size() + "\n");
        for(var dep : dm.getConflictDependencies()) {
            channels.writeToStdOut(ANSI.start().text(" - ").bold().fgGreen().text(dep.revisionId().toString()).nl().render());
        }
        return null;
    }

    Object evalLineListArtifacts(RapaioKernel kernel, ExecutionContext context, MagicSnippet snippet) {
        if (!canHandleOneLinePrefix(snippet, HEADER + " /list-artifacts")) {
            throw new RuntimeException("Cannot evaluate the given magic snippet.");
        }
        var channels = kernel.channels();
        var dm = kernel.dependencyManager();
        channels.writeToStdOut("Loaded artifacts count: " + dm.getLoadedArtifacts().size() + "\n");
        for (var report : dm.getLoadedArtifacts()) {
            channels.writeToStdOut(ANSI.start().text(" - ").bold().fgGreen().text(report.getArtifact().toString()).nl().render());
        }
        return null;
    }
}
