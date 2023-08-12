package org.rapaio.jupyter.kernel.core.magic.handlers;

import org.apache.ivy.core.report.ResolveReport;
import org.rapaio.jupyter.kernel.core.ExecutionContext;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.SnippetMagicHandler;
import org.rapaio.jupyter.kernel.core.magic.maven.DepCoordinates;
import org.rapaio.jupyter.kernel.core.magic.maven.IvyDependencies;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class MavenCoordinates extends MagicHandler {

    private static final String HEADER = "%maven";

    @Override
    public String name() {
        return "maven coordinate";
    }

    @Override
    public List<SnippetMagicHandler> snippetMagicHandlers() {
        return List.of(
                SnippetMagicHandler.lineMagic()
                        .syntaxMatcher("%maven .*")
                        .syntaxHelp(List.of("%maven group_id:artifact_id:version"))
                        .syntaxPrefix("%maven ")
                        .documentation(List.of())
                        .canHandlePredicate(this::canHandleSnippet)
                        .evalFunction(this::evalLine)
                        .build()
        );
    }

    @Override
    public List<String> helpMessage() {
        return List.of("Find and resolve a maven dependency using the standard maven coordinates: group_id, artifact_id"
                        + " and version id.",
                "The maven public repositories are searched for dependencies. Additionally, any maven transitive dependency declared with "
                        + "magic handlers are included in classpath.");
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet magicSnippet) {
        if (!magicSnippet.isLineMagic()) {
            return false;
        }
        return magicSnippet.lines().get(0).code().startsWith(HEADER);
    }

    Object evalLine(RapaioKernel kernel, ExecutionContext context, MagicSnippet snippet) {
        if (!canHandleSnippet(snippet)) {
            throw new RuntimeException("Cannot evaluate the given magic snippet.");
        }
        String fullCode = snippet.lines().get(0).code();
        String args = fullCode.substring(HEADER.length()).trim();

        try {
            DepCoordinates dc = new DepCoordinates(args);
            kernel.channels().writeToStdOut("Solving dependencies for " + ANSI.start().bold().fgGreen().text(dc.toString()).reset().render() + "\n");
            ResolveReport resolveReport = IvyDependencies.getInstance().resolve(dc);
            var adrs = resolveReport.getAllArtifactsReports();
            kernel.channels().writeToStdOut("Found dependencies count: " + adrs.length + "\n");
            for (var adr : adrs) {
                if (adr.getExt().equalsIgnoreCase("jar") && adr.getType().equalsIgnoreCase("jar")) {
                    kernel.channels().writeToStdOut("Add to classpath: " +
                            ANSI.start().fgGreen().text(adr.getLocalFile().getAbsolutePath()).reset().render() + "\n");
                    kernel.javaEngine().getShell().addToClasspath(adr.getLocalFile().getAbsolutePath());
                }
            }
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
