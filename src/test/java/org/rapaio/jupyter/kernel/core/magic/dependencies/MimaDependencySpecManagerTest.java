package org.rapaio.jupyter.kernel.core.magic.dependencies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.spi.ResolveResult;

import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MimaDependencySpecManagerTest {

    private MimaDependencyManager dm;

    @BeforeEach
    void beforeEach() {
        dm = new MimaDependencyManager();
    }

    @Test
    void testRepos() throws ParseException, IOException {

        Set<String> existingNames = dm.getMavenRepositories().stream().map(RemoteRepository::getId).collect(Collectors.toSet());

        String existingName = existingNames.stream().findAny().orElse("");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> dm.addMavenRepository(existingName, "url"));
        assertEquals("Existing maven repository: " + existingName, ex.getMessage());

        dm.addMavenRepository("google", "https://maven.google.com/");

        Set<String> newNames = dm.getMavenRepositories().stream().map(RemoteRepository::getId).collect(Collectors.toSet());
        assertTrue(existingNames.size() != newNames.size());

        assertEquals(existingNames.size(), +1, newNames.size());
        assertTrue(newNames.contains("google"));

        dm.addDependency(new DependencySpec("com.google.ar:core:pom:1.43.0", true));
        var report = dm.resolve();
        assertFalse(report.getArtifactResults().isEmpty());
        assertTrue(report.getCollectExceptions().isEmpty());
    }

    @Test
    void testUnforcedResolveConflicts() throws ParseException, IOException {

        dm.addDependency(new DependencySpec("com.google.guava:guava:20.0", true));
        dm.addDependency(new DependencySpec("com.google.inject:guice:4.2.2", true));
        dm.addDependency(new DependencySpec("com.google.guava:guava:25.1-android", true));

        var report = dm.resolve();

        assertEquals(0, report.getCollectExceptions().size());
        Set<String> moduleRevisionIds = Set.of(
                "com.google.inject:guice:jar:4.2.2",
                "javax.inject:javax.inject:jar:1",
                "aopalliance:aopalliance:jar:1.0",
                "com.google.guava:guava:jar:25.1-android",
                "com.google.code.findbugs:jsr305:jar:3.0.2",
                "org.checkerframework:checker-compat-qual:jar:2.0.0",
                "com.google.errorprone:error_prone_annotations:jar:2.1.3",
                "com.google.j2objc:j2objc-annotations:jar:1.1",
                "org.codehaus.mojo:animal-sniffer-annotations:jar:1.14"
        );
        assertEquals(moduleRevisionIds.size(), report.getArtifactResults().size());
        for (var ar : report.getArtifactResults()) {
            assertTrue(moduleRevisionIds.contains(ar.getArtifact().toString()), "Problem at: " + ar.getArtifact().toString());
        }
    }

    @Test
    void testForcedResolveConflicts() throws ParseException, IOException {

        dm.addDependency(new DependencySpec("com.google.guava:guava:20.0", true));
        dm.addDependency(new DependencySpec("com.google.inject:guice:4.2.2", false));
        dm.addDependency(new DependencySpec("com.google.guava:guava:25.1-android", false));

        var report = dm.resolve();

        assertEquals(0, report.getCollectExceptions().size());
        Set<String> moduleRevisionIds = Set.of(
                "com.google.inject:guice:jar:4.2.2",
                "javax.inject:javax.inject:jar:1",
                "aopalliance:aopalliance:jar:1.0",
                "com.google.guava:guava:jar:25.1-android",
                "com.google.code.findbugs:jsr305:jar:3.0.2",
                "org.checkerframework:checker-compat-qual:jar:2.0.0",
                "com.google.errorprone:error_prone_annotations:jar:2.1.3",
                "com.google.j2objc:j2objc-annotations:jar:1.1",
                "org.codehaus.mojo:animal-sniffer-annotations:jar:1.14"
        );
        assertEquals(moduleRevisionIds.size(), report.getArtifactResults().size());
        for (var ar : report.getArtifactResults()) {
            assertTrue(moduleRevisionIds.contains(ar.getArtifact().toString()),
                    "%s artifact not found".formatted(ar.getArtifact().toString()));
        }
    }

    @Test
    @Disabled
    void testConfigurationMappings() throws ParseException, IOException {
//        MimaDependencyManager dm = new MimaDependencyManager();
//        Ivy ivy = dm.getIvy();
//        var md = dm.getMd();
//
//        DependencySpec dependencySpec = DependencySpec.from("org.lwjgl:lwjgl-glfw:3.3.3", true);
//
//        ModuleRevisionId ri = dependencySpec.revisionId();
//        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md, ri, dependencySpec.force(), true, true);
//        dd.addDependencyConfiguration("default", "default");
//        dd.addDependencyArtifact("default", new DefaultDependencyArtifactDescriptor(
//                dd, dependencySpec.name(), "jar", "jar", null, Map.of("e:classifier", "natives-macos-arm64")
//        ));
//
//        md.addDependency(dd);
//        md.setLastModified(System.currentTimeMillis());
//
//        ResolveOptions ro = new ResolveOptions();
//        ro.setTransitive(true);
//        ro.setDownload(true);
//
//        var report = ivy.resolve(md, ro);
//        assertEquals(0, report.getAllProblemMessages().size());
//        assertEquals(2, report.getAllArtifactsReports().length);
    }

    @Test
    void testArtifactWithExtra() throws ParseException, IOException {
        DependencySpec dependencySpec = new DependencySpec("org.lwjgl:lwjgl-glfw:jar:natives-macos-arm64:3.3.3", false);
        dm.addDependency(dependencySpec);
        DependencyResult report = dm.resolve();
        assertNotNull(report.getArtifactResults());
        assertEquals(2, report.getArtifactResults().size());
        assertTrue(report.getArtifactResults().getFirst().getLocalArtifactResult().getFile()
                        .getAbsolutePath().endsWith("lwjgl-glfw-3.3.3-natives-macos-arm64.jar"));
        assertTrue(report.getArtifactResults().get(1).getLocalArtifactResult().getFile()
                .getAbsolutePath().endsWith("lwjgl-3.3.3.jar"));
    }
}