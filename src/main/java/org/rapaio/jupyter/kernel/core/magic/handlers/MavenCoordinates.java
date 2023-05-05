package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.apache.ivy.core.report.ResolveReport;
import org.rapaio.jupyter.kernel.channels.ReplyEnv;
import org.rapaio.jupyter.kernel.core.ReplacementOptions;
import org.rapaio.jupyter.kernel.core.display.text.ANSI;
import org.rapaio.jupyter.kernel.core.java.JavaEngine;
import org.rapaio.jupyter.kernel.core.magic.MagicEvaluator;
import org.rapaio.jupyter.kernel.core.magic.MagicHandler;
import org.rapaio.jupyter.kernel.core.magic.MagicParseException;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;
import org.rapaio.jupyter.kernel.core.magic.maven.DepCoordinates;
import org.rapaio.jupyter.kernel.core.magic.maven.IvyDependencies;

public class MavenCoordinates implements MagicHandler {

    private static final String HEADER = "%maven ";

    @Override
    public String name() {
        return "maven coordinate";
    }

    @Override
    public String syntax() {
        return "%maven group_id:artifact_id:version";
    }

    @Override
    public List<String> helpMessage() {

        return List.of("Find and resolve a maven dependency using the standard maven coordinates: group_id, artifact_id"
                        + " and version id.",
                "The maven public repositories are searched for dependencies. Additionally, any maven repository declared with "
                        + "magic handlers are included in search.");
    }

    @Override
    public boolean canHandleSnippet(MagicSnippet snippet) {
        if (!snippet.oneLine()) {
            return false;
        }
        return snippet.lines().get(0).code().startsWith(HEADER);
    }

    @Override
    public Object eval(MagicEvaluator magicEvaluator, JavaEngine javaEngine, ReplyEnv env, MagicSnippet snippet) throws MagicParseException {
        if (!canHandleSnippet(snippet)) {
            throw new RuntimeException("Cannot evaluate the given magic snippet.");
        }
        String fullCode = snippet.lines().get(0).code();
        String args = fullCode.substring(HEADER.length()).trim();

        try {
            DepCoordinates dc = new DepCoordinates(args);
            env.writeToStdOut("Solving dependencies for " + ANSI.start().bold().fgGreen().text(dc.toString()).reset().build() + "\n");
            ResolveReport resolveReport = IvyDependencies.getInstance().resolve(dc);
            var adrs = resolveReport.getAllArtifactsReports();
            env.writeToStdOut("Found dependencies count: " + adrs.length + "\n");
            for (var adr : adrs) {
                if (adr.getExt().equalsIgnoreCase("jar") && adr.getType().equalsIgnoreCase("jar")) {
                    env.writeToStdOut("Add to classpath: " +
                            ANSI.start().fgGreen().text(adr.getLocalFile().getAbsolutePath()).reset().build() + "\n");
                    javaEngine.getShell().addToClasspath(adr.getLocalFile().getAbsolutePath());
                }
            }
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public ReplacementOptions complete(ReplyEnv env, MagicSnippet snippet) {
        return null;
    }
}
