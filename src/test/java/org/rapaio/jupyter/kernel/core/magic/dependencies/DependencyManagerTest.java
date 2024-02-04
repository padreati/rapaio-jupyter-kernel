package org.rapaio.jupyter.kernel.core.magic.dependencies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.DefaultDependencyArtifactDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.core.RapaioKernel;

public class DependencyManagerTest {

    private RapaioKernel kernel;

    @BeforeEach
    void beforeEach() {
        kernel = new RapaioKernel();
    }

    @Test
    void testUnforcedResolveConflicts() throws ParseException, IOException {

        var dm = kernel.dependencyManager();

        dm.setLatestRevisionConflictManager();
        dm.addDependency(new Dependency("com.google.guava:guava:20.0", false));
        dm.addDependency(new Dependency("com.google.inject:guice:4.2.2", false));
        dm.addOverrideDependency(new Dependency("com.google.guava:guava:25.1-android", false));

        var report = dm.resolve();

        assertEquals(0, report.getAllProblemMessages().size());
        Set<String> moduleRevisionIds = Set.of(
                "com.google.inject#guice;4.2.2",
                "javax.inject#javax.inject;1",
                "aopalliance#aopalliance;1.0",
                "com.google.guava#guava;25.1-android",
                "com.google.code.findbugs#jsr305;3.0.1",
                "org.checkerframework#checker-compat-qual;2.0.0",
                "com.google.errorprone#error_prone_annotations;2.1.3",
                "com.google.j2objc#j2objc-annotations;1.1",
                "org.codehaus.mojo#animal-sniffer-annotations;1.14",
                "com.google.code.findbugs#jsr305;3.0.2"
        );
        assertEquals(moduleRevisionIds.size(), report.getAllArtifactsReports().length);
        for (var ar : report.getAllArtifactsReports()) {
            assertTrue(moduleRevisionIds.contains(ar.getArtifact().getModuleRevisionId().toString()));
        }
    }

    @Test
    void testForcedResolveConflicts() throws ParseException, IOException {

        var dm = kernel.dependencyManager();

        dm.setLatestRevisionConflictManager();
        dm.addDependency(new Dependency("com.google.guava:guava:20.0", true));
        dm.addDependency(new Dependency("com.google.inject:guice:4.2.2", true));
        dm.addOverrideDependency(new Dependency("com.google.guava:guava:25.1-android", false));

        var report = dm.resolve();

        assertEquals(0, report.getAllProblemMessages().size());
        Set<String> moduleRevisionIds = Set.of(
                "com.google.inject#guice;4.2.2",
                "javax.inject#javax.inject;1",
                "aopalliance#aopalliance;1.0",
                "com.google.guava#guava;25.1-android",
                "com.google.guava#guava;20.0",
                "com.google.code.findbugs#jsr305;3.0.1",
                "org.checkerframework#checker-compat-qual;2.0.0",
                "com.google.errorprone#error_prone_annotations;2.1.3",
                "com.google.j2objc#j2objc-annotations;1.1",
                "org.codehaus.mojo#animal-sniffer-annotations;1.14"
        );
        assertEquals(moduleRevisionIds.size(), report.getAllArtifactsReports().length);
        for (var ar : report.getAllArtifactsReports()) {
            assertTrue(moduleRevisionIds.contains(ar.getArtifact().getModuleRevisionId().toString()),
                    "%s artifact not found".formatted(ar.getArtifact().getModuleRevisionId()));
        }
    }

    @Test
    void testRepos() {
        var dm = kernel.dependencyManager();

        Set<String> existingNames = dm.getResolver().getResolvers().stream().map(DependencyResolver::getName).collect(Collectors.toSet());

        String existingName = existingNames.stream().findAny().orElse("");
        assertThrows(RuntimeException.class, () -> dm.addMavenRepository(existingName, "url"));

        dm.addMavenRepository("google", "https://maven.google.com/");

        Set<String> newNames = dm.getResolver().getResolvers().stream().map(DependencyResolver::getName).collect(Collectors.toSet());
        assertTrue(existingNames.size() != newNames.size());

        assertEquals(existingNames.size(), +1, newNames.size());
        assertTrue(newNames.contains("google"));
    }

    @Test
    void testConfigurationMappings() throws ParseException, IOException {
        DependencyManager dm = new DependencyManager();
        Ivy ivy = dm.getIvy();
        var md = dm.getMd();

        Dependency dependency = new Dependency("org.lwjgl:lwjgl-glfw:3.3.3", true);

        ModuleRevisionId ri = dependency.revisionId();
        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md, ri, dependency.force(), true, true);
        dd.addDependencyConfiguration("default", "default");
        dd.addDependencyArtifact("default", new DefaultDependencyArtifactDescriptor(
                dd, dependency.artifactId(), "jar", "jar", null, Map.of("e:classifier", "natives-macos-arm64")));


        md.addDependency(dd);
        md.setLastModified(System.currentTimeMillis());

        ResolveOptions ro = new ResolveOptions();
        ro.setTransitive(true);
        ro.setDownload(true);

        var report = ivy.resolve(md, ro);
        for (var msg : report.getAllProblemMessages()) {
            System.out.println(msg);
        }

        for (var art : report.getAllArtifactsReports()) {
            System.out.println(art.getLocalFile().getAbsolutePath());
        }
    }
}

// https://repo.maven.apache.org/maven2/org/lwjgl/lwjgl-glfw/3.3.3/lwjgl-glfw-3.3.3-natives-macos-arm64.jar
// https://repo.maven.apache.org/maven2/org/lwjgl/lwjgl-glfw/3.3.3/lwjgl-glfw-3.3.3-native-macos-arm64.jar