package org.rapaio.jupyter.kernel.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.rapaio.jupyter.kernel.core.ExecutionContext;

public class GlobTest {

    @TempDir
    private Path tempRoot1;
    @TempDir
    private Path tempRoot2;

    private Path jar1;
    private Path jar2;

    @BeforeEach
    void beforeEach() throws IOException {
        Path target = tempRoot1.resolve("target");
        target.toFile().mkdirs();

        jar1 = target.resolve("jar1.jar");
        jar1.toFile().createNewFile();
        jar2 = target.resolve("jar2.jar");
        jar2.toFile().createNewFile();

        Path classes = tempRoot1.resolve("classes");
        classes.toFile().mkdirs();

        Path subclasses = classes.resolve("subclasses");
        subclasses.toFile().mkdirs();

        Path class1 = subclasses.resolve("class1.class");
        class1.toFile().createNewFile();
        Path class2 = subclasses.resolve("class2.class");
        class2.toFile().createNewFile();
        Path class3 = subclasses.resolve("class3.class");
        class3.toFile().createNewFile();
    }

    @Test
    void testGlobFindJars() throws IOException {
        ExecutionContext ctx = new ExecutionContext(tempRoot1.toAbsolutePath().toString());
        Glob glob = new Glob();
        List<Path> paths = glob.findPaths(ctx, "target/*.jar");
        assertEquals(2, paths.size());
        assertTrue(paths.contains(jar1));
        assertTrue(paths.contains(jar2));
    }

    @Test
    void testDirectory() throws IOException {
        ExecutionContext ctx = new ExecutionContext(tempRoot1.toAbsolutePath().toString());
        List<Path> paths = new Glob().findPaths(ctx, "target");
        assertEquals(1, paths.size());
        assertTrue(paths.contains(tempRoot1.resolve("target")));
    }

    @Test
    void testDirectoryWithContent() throws IOException {
        ExecutionContext ctx = new ExecutionContext(tempRoot1.toAbsolutePath().toString());
        List<Path> paths = new Glob().findPaths(ctx, "classes/**/*.class");
        assertEquals(3, paths.size());
    }

    @Test
    void testWrongDir() throws IOException {
        ExecutionContext ctx = new ExecutionContext(tempRoot2.toAbsolutePath().toString());
        List<Path> paths = new Glob().findPaths(ctx, "target");
        assertEquals(0, paths.size());
    }
}
