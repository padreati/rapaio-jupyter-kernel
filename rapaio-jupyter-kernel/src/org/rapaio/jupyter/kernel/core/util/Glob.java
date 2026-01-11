package org.rapaio.jupyter.kernel.core.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rapaio.jupyter.kernel.core.ExecutionContext;

public class Glob {

    public List<Path> findPaths(ExecutionContext ctx, String classpath) throws IOException {
        if (classpath == null || classpath.isEmpty()) {
            return List.of();
        }

        String[] tokens = classpath.split(FileSystems.getDefault().getSeparator());
        for (int i = 0; i < tokens.length - 1; i++) {
            tokens[i] = tokens[i] + FileSystems.getDefault().getSeparator();
        }
        int nonMasked = 0;
        for (String token : tokens) {
            if (token.contains("*") || token.contains("?") || token.contains("[") || token.contains("]")) {
                break;
            }
            nonMasked++;
        }
        String prefix = String.join("", Arrays.copyOfRange(tokens, 0, nonMasked));
        String suffix = String.join("", Arrays.copyOfRange(tokens, nonMasked, tokens.length));
        Path root;
        PathMatcher matcher;
        if (Path.of(prefix).isAbsolute()) {
            root = Path.of(prefix);
            matcher = FileSystems.getDefault().getPathMatcher("glob:" + classpath);
        } else {
            root = ctx.getRootPath().resolve(prefix);
            matcher = FileSystems.getDefault()
                    .getPathMatcher("glob:" + root.toAbsolutePath() + (suffix.isEmpty() ? "" : root.getFileSystem().getSeparator() + suffix));
        }
        List<Path> paths = new ArrayList<>();
        recursiveSearch(paths, root, matcher);
        return paths;
    }

    private void recursiveSearch(List<Path> list, Path root, PathMatcher matcher) {
        if (matcher.matches(root.toAbsolutePath()) && root.toFile().exists()) {
            list.add(root);
        }
        if (root.toFile().isDirectory()) {
            String[] children = root.toFile().list();
            if (children != null) {
                for (String child : children) {
                    recursiveSearch(list, root.resolve(child), matcher);
                }
            }
        }
    }
}