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
import org.rapaio.jupyter.kernel.core.magic.handlers.RepoParam;

import eu.maveniverse.maven.mima.context.Context;
import eu.maveniverse.maven.mima.context.ContextOverrides;
import eu.maveniverse.maven.mima.context.Runtimes;
import org.rapaio.jupyter.kernel.install.Installer;

public class MimaDependencyManager {

    private final ContextOverrides contextOverrides;
    private final List<RemoteRepository> remoteRepositories;

    private final List<DependencySpec> proposedDependencies = new ArrayList<>();
    private final List<DependencySpec> resolvedDependencies = new ArrayList<>();
    private final List<ArtifactResult> loadedArtifacts = new ArrayList<>();

    public MimaDependencyManager() {

        String kernelPath = MimaDependencyManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if(Installer.findOSName()== Installer.OSName.WINDOWS) {
            if(kernelPath.startsWith("/")) {
                kernelPath = kernelPath.substring(1);
            }
        }
        this.contextOverrides = ContextOverrides.create()
                .withLocalRepositoryOverride(Path.of(kernelPath.substring(0, kernelPath.lastIndexOf('/')), "mima_cache"))
                .snapshotUpdatePolicy(ContextOverrides.SnapshotUpdatePolicy.ALWAYS).build();
        this.remoteRepositories = new ArrayList<>();
        remoteRepositories.add(ContextOverrides.CENTRAL);
        // this one is gone https://jfrog.com/blog/jcenter-sunset/
        //addMavenRepository("jcenter", "https://jcenter.bintray.com/",
        //        RepoParam.defaultRemote()); //
        addMavenRepository("jboss", "https://repository.jboss.org/nexus/content/repositories/releases/",
                RepoParam.defaultRemote());
        addMavenRepository("atlassian", "https://packages.atlassian.com/maven/public",
                RepoParam.defaultRemote());
    }

    public void addMavenRepository(String id, String url, RepoParam repoParam) {
        Set<String> repositoryIds = remoteRepositories.stream().map(RemoteRepository::getId).collect(Collectors.toSet());
        if (repositoryIds.contains(id)) {
            throw new RuntimeException("Existing maven repository: " + id);
        }
        RemoteRepository remoteRepository = (new RemoteRepository.Builder(id, "default", url))
                .setReleasePolicy(new RepositoryPolicy(repoParam.release(), repoParam.releaseUpdate(), "warn"))
                .setSnapshotPolicy(new RepositoryPolicy(repoParam.snapshot(), repoParam.snapshotUpdate(), "warn"))
                .build();
        remoteRepositories.add(remoteRepository);
    }

    public List<RemoteRepository> getMavenRepositories() {
        return remoteRepositories;
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
        try (Context context = Runtimes.INSTANCE.getRuntime().create(
                contextOverrides.toBuilder()
                        .repositories(remoteRepositories)
                        .addRepositoriesOp(ContextOverrides.AddRepositoriesOp.REPLACE)
                        .build())) {
            List<Dependency> dependencies = new ArrayList<>();

            for (DependencySpec ds : this.proposedDependencies) {
                dependencies.add(ds.getDependency());
            }

            CollectRequest collectRequest = new CollectRequest((Dependency) null, dependencies, context.remoteRepositories());
            DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);

            return context.repositorySystem().resolveDependencies(context.repositorySystemSession(), dependencyRequest);
        }
    }

    public List<ArtifactResult> getLoadedArtifacts() {
        return loadedArtifacts;
    }

    public void addLoadedArtifact(ArtifactResult result) {
        loadedArtifacts.add(result);
    }
}
