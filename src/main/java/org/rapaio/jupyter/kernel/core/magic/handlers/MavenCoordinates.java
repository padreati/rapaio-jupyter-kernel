package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.apache.ivy.core.report.ResolveReport;
import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.RapaioKernel;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicParseException;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.OneLineMagicHandler;
import org.rapaio.jupyter.kernel.core.magic.maven.DepCoordinates;
import org.rapaio.jupyter.kernel.core.magic.maven.IvyDependencies;

public class MavenCoordinates implements MagicHandler {

    private static final String HEADER = "%maven ";

    @Override
    public String name() {
        return "maven coordinate";
    }

    @Override
    public List<OneLineMagicHandler> oneLineMagicHandlers() {
        return List.of(
                OneLineMagicHandler.builder()
                        .syntaxMatcher("%maven .*")
                        .syntaxHelp("%maven group_id:artifact_id:version")
                        .documentation(List.of())
                        .canHandlePredicate(this::canHandleSnippet)
                        .evalFunction(this::evalLine)
                        .inspectFunction((channels, magicSnippet) -> null)
                        .completeFunction(this::complete)
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
    public boolean canHandleSnippet(MagicSnippet snippet) {
        if (!snippet.oneLine()) {
            return false;
        }
        return snippet.lines().get(0).code().startsWith(HEADER);
    }

    public Object evalLine(RapaioKernel kernel, MagicSnippet snippet) throws MagicParseException {
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

    @Override
    public CompleteMatches complete(Channels channels, MagicSnippet snippet) {
        return null;
    }
}
