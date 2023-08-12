package org.rapaio.jupyter.kernel.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ExecutionContext {

    private final Path rootPath;

    public ExecutionContext(String notebookPath) {
        if(notebookPath==null) {
            rootPath = Paths.get("").toAbsolutePath();
        } else {
            Path path = Path.of(notebookPath).toAbsolutePath();
            if(!Files.exists(path)) {
                throw new IllegalArgumentException("Given notebook path is invalid.");
            }
            if(!Files.isDirectory(path)) {
                path = path.getParent();
            }
            rootPath = path;
        }
    }

    public Path getRootPath() {
        return rootPath;
    }

    public Path getRelativePath(Path path) {
        return rootPath.resolve(path);
    }
}
