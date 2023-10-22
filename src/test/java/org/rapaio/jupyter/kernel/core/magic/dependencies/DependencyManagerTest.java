package org.rapaio.jupyter.kernel.core.magic.dependencies;

import java.io.IOException;
import java.text.ParseException;

import org.junit.jupiter.api.Test;

public class DependencyManagerTest {

    @Test
    void testConflict() throws ParseException, IOException {

        DependencyManager ivy = DependencyManager.getInstance();

        DependencyManager.setLatestRevisionConflictManager();


        ivy.addDependency(new Dependency("com.google.guava:guava:20.0", false));
        ivy.addDependency(new Dependency("com.google.inject:guice:4.2.2", false));
        ivy.addOverrideDependency(new Dependency("com.google.guava:guava:25.1-android", false));

        var report = ivy.resolve();
        System.out.println(report.getAllProblemMessages());
        for (var ar : report.getAllArtifactsReports()) {
            System.out.println(ar);
        }
    }
}
