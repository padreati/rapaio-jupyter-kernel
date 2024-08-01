package org.rapaio.jupyter.kernel.core.magic.dependencies;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;

import eu.maveniverse.maven.mima.context.Context;
import eu.maveniverse.maven.mima.context.ContextOverrides;
import eu.maveniverse.maven.mima.context.Runtimes;

public class MimaDependencyManager {

    private final Context context;

    private final List<DependencySpec> proposedDependencies = new ArrayList<>();
    private final List<DependencySpec> resolvedDependencies = new ArrayList<>();
    private final List<ArtifactResult> loadedArtifacts = new ArrayList<>();

    public MimaDependencyManager() {

        String kernelPath = MimaDependencyManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        ContextOverrides contextOverrides = ContextOverrides.create()
                .withLocalRepositoryOverride(Path.of(kernelPath.substring(0, kernelPath.lastIndexOf('/')), "mima_cache"))
                .snapshotUpdatePolicy(ContextOverrides.SnapshotUpdatePolicy.ALWAYS)
                .build();
        context = Runtimes.INSTANCE.getRuntime().create(contextOverrides);
        addMavenRepository("jcenter", "https://jcenter.bintray.com/");
        addMavenRepository("jboss", "https://repository.jboss.org/nexus/content/repositories/releases/");
        addMavenRepository("atlassian", "https://packages.atlassian.com/maven/public");
    }

    public void addMavenRepository(String id, String url) {

        Set<String> repositoryIds = context.remoteRepositories().stream().map(RemoteRepository::getId).collect(Collectors.toSet());
        if (repositoryIds.contains(id)) {
            throw new RuntimeException("Existing maven repository: " + id);
        }
        RemoteRepository remoteRepository = (new RemoteRepository.Builder(id, "default", url))
                .setReleasePolicy(new RepositoryPolicy(true, "always", "warn"))
                .setSnapshotPolicy(new RepositoryPolicy(true, "always", "warn"))
                .build();
        context.remoteRepositories().add(remoteRepository);

    }

    public List<RemoteRepository> getMavenRepositories() {
        return context.remoteRepositories();
    }

    public List<DependencySpec> getProposedDependencies() {
        return Collections.unmodifiableList(proposedDependencies);
    }

    public List<DependencySpec> getResolvedDependencies() {
        return Collections.unmodifiableList(resolvedDependencies);
    }

    public void cleanProposedDependencies() {
        proposedDependencies.clear();
    }

    public void proposeDependency(DependencySpec dependencySpec) {
        proposedDependencies.add(dependencySpec);
    }

    public void promoteDependencies() {
        resolvedDependencies.addAll(proposedDependencies);
    }

    public DependencyResult resolve() throws ParseException, IOException, DependencyResolutionException {
        List<Dependency> dependencies = new ArrayList<>();

        for (DependencySpec ds : this.proposedDependencies) {
            dependencies.add(ds.getDependency());
        }

        Dependency root = null;
        CollectRequest collectRequest = new CollectRequest(root, dependencies, context.remoteRepositories());
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);

        context.repositorySystem().resolveDependencies(context.repositorySystemSession(), dependencyRequest);

        // This is intentionally repeated!
        // I do not understand why at first try the local artifacts are missing, I suppose it is due to the
        // my lack of understanding on how maven works
        // However, at the second time the local files are properly updated and collected
        return context.repositorySystem().resolveDependencies(context.repositorySystemSession(), dependencyRequest);
    }

    public List<ArtifactResult> getLoadedArtifacts() {
        return loadedArtifacts;
    }

    public void addLoadedArtifact(ArtifactResult result) {
        loadedArtifacts.add(result);
    }
}
