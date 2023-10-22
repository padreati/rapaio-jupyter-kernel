package org.rapaio.jupyter.kernel.core.magic.dependencies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.util.Set;

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


}
