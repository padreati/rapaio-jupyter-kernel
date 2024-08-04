package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.SnippetMagicHandler;
import org.rapaio.jupyter.kernel.core.magic.dependencies.DependencySpec;

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
                        .syntaxMatcher(HEADER + " /list-repos")
                        .syntaxHelp(List.of(HEADER + " /list-repos"))
                        .syntaxPrefix(HEADER + " /list-repos")
                        .documentation(List.of(
                                "List all repositories"
                        ))
                        .canHandlePredicate(snippet -> canHandleOneLinePrefix(snippet, HEADER + " /list-repos"))
                        .evalFunction(this::evalLineListRepos)
                        .build(),
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher(HEADER + " /add-repo (.+) (.+) (.+)?")
                        .syntaxHelp(List.of(HEADER + " /add-repo name url[ release|update-policy]?[ snapshot|update-policy]?"))
                        .syntaxPrefix(HEADER + " /add-repo ")
                        .documentation(List.of(
                                "Add Maven Repository using a name and an url. Optional parameters are available. ",
                                "    Release enabled is obtained by specifying release|update-policy.",
                                "    Update policy can be: never/always/daily/interval.",
                                "    Snapshot enabled is obtained by specified snapshot|update-policy.",
                                "    Update policy can be: never/always/daily/interval.",
                                "    If any flag is not specified we have the following default options:",
                                "     - release enabled with never, snapshot enabled with never for repos with url starting with file://",
                                "     - release enabled with never, snapshot disabled for repos with url not starting with file://"
                        ))
                        .canHandlePredicate(snippet -> canHandleOneLinePrefix(snippet, HEADER + " /add-repo "))
                        .evalFunction(this::evalLineAddRepo)
                        .build(),
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher(HEADER + " /list-dependencies")
                        .syntaxHelp(List.of(HEADER + " /list-dependencies"))
                        .syntaxPrefix(HEADER + " /list-dependencies")
                        .documentation(List.of(
                                "List proposed and resolved dependencies"
                        ))
                        .canHandlePredicate(snippet -> canHandleOneLinePrefix(snippet, HEADER + " /list-dependencies"))
                        .evalFunction(this::evalLineListDependencies)
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
                        .syntaxMatcher(HEADER + " /resolve")
                        .syntaxHelp(List.of(HEADER + " /resolve"))
                        .syntaxPrefix(HEADER + " /resolve")
                        .documentation(List.of("Resolve dependencies"))
                        .canHandlePredicate(snippet -> canHandleOneLinePrefix(snippet, HEADER + " /resolve"))
                        .evalFunction(this::evalLineResolve)
                        .build(),
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher(HEADER + " /add( \\S+)+ (\\-\\-optional)*")
                        .syntaxHelp(List.of(
                                HEADER + " /add groupId:artifactId[:extension[:classifier]]:version",
                                HEADER + " /add groupId:artifactId[:extension[:classifier]]:version --optional"
                        ))
                        .syntaxPrefix(HEADER + " /add")
                        .documentation(List.of(
                                "Declares a dependency to the current notebook. Artifact's extension (default value jar) and "
                                        + "classifier (default value is empty string) values are optional."
                        ))
                        .canHandlePredicate(snippet -> canHandleOneLinePrefix(snippet, HEADER + " /add "))
                        .evalFunction(this::evalLineAdd)
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

    Object evalLineListRepos(RapaioKernel kernel, MagicSnippet snippet) {
        if (!canHandleOneLinePrefix(snippet, HEADER + " /list-repos")) {
            throw new RuntimeException("Cannot evaluate the given magic snippet");
        }

        var channels = kernel.channels();
        var dm = kernel.dependencyManager();

        channels.writeToStdOut("Repositories count: " + dm.getMavenRepositories().size() + "\n");
        for (var resolver : dm.getMavenRepositories()) {
            String name = resolver.getId();
            String url = resolver.getUrl();

            RepositoryPolicy releasePolicy = resolver.getPolicy(false);
            RepositoryPolicy snapshotPolicy = resolver.getPolicy(true);

            channels.writeToStdOut(ANSI
                    .start().text("name: ").bold().fgGreen().text(name + " ")
                    .reset().text("url: ").bold().fgGreen().text(url + " ")
                    .reset().text("release:").fgGreen().text(releasePolicy.isEnabled() + " ")
                    .reset().text("update:").fgGreen().text(releasePolicy.getUpdatePolicy() + " ")
                    .reset().text("snapshot:").fgGreen().text(snapshotPolicy.isEnabled() + " ")
                    .reset().text("update:").fgGreen().text(snapshotPolicy.getUpdatePolicy() + " ")
                    .nl()
                    .render());
        }
        return null;
    }

    Object evalLineAddRepo(RapaioKernel kernel, MagicSnippet snippet) {
        if (!canHandleOneLinePrefix(snippet, HEADER + " /add-repo ")) {
            throw new RuntimeException("Cannot evaluate the given magic snippet");
        }

        String fullCode = snippet.line(0).code();
        String[] tokens = fullCode.substring((HEADER + " /add-repo ").length()).trim().split(" ");
        if (tokens.length < 2) {
            throw new RuntimeException("Cannot evaluate the given magic snippet");
        }

        var channels = kernel.channels();
        var dm = kernel.dependencyManager();

        dm.addMavenRepository(tokens[0], tokens[1], parseRepoParams(tokens));
        channels.writeToStdOut(ANSI.start().text("Repository ").bold().fgGreen().text(tokens[0]).reset()
                .text(" url: ").bold().fgGreen().text(tokens[1]).reset().text(" added.").nl().render());

        return null;
    }

    private RepoParam parseRepoParams(String[] tokens) {
        boolean local = tokens[1].startsWith("file://");
        RepoParam param = local ? RepoParam.defaultLocal() : RepoParam.defaultRemote();
        if (tokens.length == 2) {
            return param;
        }
        for (int i = 2; i < tokens.length; i++) {
            String[] options = Arrays.stream(tokens[i].split("\\|")).map(String::trim).map(String::toLowerCase).toArray(String[]::new);
            String update = options.length > 1 ? options[1] : "always";
            boolean validUpdate = update.equals(RepositoryPolicy.UPDATE_POLICY_NEVER) ||
                    update.equals(RepositoryPolicy.UPDATE_POLICY_ALWAYS) ||
                    update.equals(RepositoryPolicy.UPDATE_POLICY_DAILY) ||
                    update.equals(RepositoryPolicy.UPDATE_POLICY_INTERVAL);
            switch (options[0]) {
                case "release":
                    if (validUpdate) {
                        param = new RepoParam(true, update, param.snapshot(), param.snapshotUpdate());
                    } else {
                        throw new RuntimeException("Invalid update policy: " + update);
                    }
                    break;
                case "snapshot":
                    if (validUpdate) {
                        param = new RepoParam(param.release(), param.releaseUpdate(), true, update);
                    } else {
                        throw new RuntimeException("Invalid update policy: " + update);
                    }
                    break;
                default:
                    throw new RuntimeException("Invalid flag: " + update + ". Valid values are release or snapshot.");
            }
        }
        return param;
    }

    Object evalLineAdd(RapaioKernel kernel, MagicSnippet snippet) {
        if (!canHandleOneLinePrefix(snippet, HEADER + " /add ")) {
            throw new RuntimeException("Cannot evaluate the given magic snippet.");
        }
        String fullCode = snippet.line(0).code();
        String[] args = fullCode.substring((HEADER + " /add ").length()).trim().split(" ");

        if (args.length == 2 && !args[1].equals("--optional")) {
            throw new RuntimeException("Error parsing '" + fullCode + "'");
        }

        kernel.channels().writeToStdOut("Adding dependency " + ANSI.start().bold().fgGreen().text(args[0]).nl().render() + "\n");
        kernel.dependencyManager().proposeDependency(new DependencySpec(args[0], null, "runtime", args.length == 2, null));
        return null;
    }

    Object evalLineResolve(RapaioKernel kernel, MagicSnippet snippet) {
        if (!canHandleOneLinePrefix(snippet, HEADER + " /resolve")) {
            throw new RuntimeException("Cannot evaluate the given magic snippet.");
        }

        try {
            if (kernel.dependencyManager().getProposedDependencies().isEmpty()) {
                kernel.channels().writeToStdOut("No proposed dependency to be solved.");
                return null;
            }

            kernel.channels().writeToStdOut("Solving dependencies\n");
            DependencyResult resolveReport = kernel.dependencyManager().resolve();
            for (var exception : resolveReport.getCollectExceptions()) {
                kernel.channels().writeToStdErr(exception.getMessage());
            }

            if (!resolveReport.getCollectExceptions().isEmpty()) {
                // we have an error
                kernel.channels().writeToStdErr("Proposed dependencies could not be resolved, clear proposals.");
                kernel.dependencyManager().cleanProposedDependencies();
                return null;
            }

            var artifactsResults = resolveReport.getArtifactResults();
            kernel.channels().writeToStdOut("Resolved artifacts count: " + artifactsResults.size() + "\n");
            for (var result : artifactsResults) {
                kernel.channels().writeToStdOut("Add to classpath: " +
                        ANSI.start().fgGreen().text(result.getLocalArtifactResult().getFile().getAbsolutePath()).reset().nl().render() + "\n");
                kernel.javaEngine().getShell().addToClasspath(result.getLocalArtifactResult().getFile().getAbsolutePath());
                kernel.dependencyManager().addLoadedArtifact(result);
            }
            kernel.dependencyManager().promoteDependencies();
            kernel.dependencyManager().cleanProposedDependencies();
        } catch (ParseException | IOException | DependencyResolutionException e) {
            kernel.dependencyManager().cleanProposedDependencies();
            kernel.channels().writeToStdErr("Could not resolve dependencies.");
            kernel.channels().writeToStdErr(e.getMessage());
        }
        return null;
    }

    Object evalLineListArtifacts(RapaioKernel kernel, MagicSnippet snippet) {
        if (!canHandleOneLinePrefix(snippet, HEADER + " /list-artifacts")) {
            throw new RuntimeException("Cannot evaluate the given magic snippet.");
        }
        var channels = kernel.channels();
        var dm = kernel.dependencyManager();
        channels.writeToStdOut("Loaded artifacts count: " + dm.getLoadedArtifacts().size() + "\n");
        for (var la : dm.getLoadedArtifacts()) {
            channels.writeToStdOut(ANSI.start().text(" - ").bold().fgGreen().text(la.getArtifact().toString()).nl().render());
        }
        return null;
    }

    Object evalLineListDependencies(RapaioKernel kernel, MagicSnippet snippet) {
        if (!canHandleOneLinePrefix(snippet, HEADER + " /list-dependencies")) {
            throw new RuntimeException("Cannot evaluate the given magic snippet.");
        }
        var channels = kernel.channels();
        var dm = kernel.dependencyManager();
        channels.writeToStdOut("Proposed dependencies count: " + dm.getProposedDependencies().size() + "\n");
        for (var dep : dm.getProposedDependencies()) {
            channels.writeToStdOut(ANSI.start().text(" - ").bold().fgGreen().text(dep.getDependency().toString()).nl().render());
        }
        channels.writeToStdOut("Resolved dependencies count: " + dm.getResolvedDependencies().size() + "\n");
        for (var dep : dm.getResolvedDependencies()) {
            channels.writeToStdOut(ANSI.start().text(" - ").bold().fgGreen().text(dep.getDependency().toString()).nl().render());
        }
        return null;
    }

}
