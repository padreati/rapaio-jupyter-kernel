package org.rapaio.jupyter.kernel.core.magic.dependencies;

import java.util.Collection;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.Exclusion;

public class DependencySpec {

    private final String coords;
    private final Dependency dependency;

    public DependencySpec(String coords, boolean optional) {
        this(coords, null, "runtime", optional, null);
    }

    public DependencySpec(String coords, Map<String, String> properties, String scope, boolean optional, Collection<Exclusion> exclusions) {
        Artifact artifact = new DefaultArtifact(coords, properties);
        this.coords = coords;
        this.dependency = new Dependency(artifact, scope, optional, exclusions);
    }

    public DependencySpec(Artifact artifact, String scope, boolean optional, Collection<Exclusion> exclusions) {
        this.coords = artifact.toString();
        this.dependency = new Dependency(artifact, scope, optional, exclusions);
    }

    public Artifact getArtifact() {
        return dependency.getArtifact();
    }

    public Dependency getDependency() {
        return dependency;
    }

    @Override
    public String toString() {
        return dependency.getArtifact().toString();
    }
}
