package org.rapaio.jupyter.kernel.core.magic.handlers;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rapaio.jupyter.kernel.core.Suggestions;
import org.rapaio.jupyter.kernel.core.magic.MagicSnippet;

public final class HandlerUtils {

    private HandlerUtils() {
    }

    public static Suggestions oneLinePathComplete(String prefix, MagicSnippet snippet, FileFilter fileFilter) {
        var line = snippet.lines().get(0);
        if (!line.hasPosition()) {
            return null;
        }
        String code = line.code();
        if (line.relativePosition() >= prefix.length()) {
            String path = code.substring(prefix.length(), line.relativePosition());

            int indexPos = path.lastIndexOf('/') + 1;
            String pathPrefix = path.substring(indexPos);
            path = path.substring(0, indexPos);
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                File[] children = file.listFiles(f -> f.getName().startsWith(pathPrefix) && (fileFilter.accept(f)));
                if (children != null) {
                    List<String> options =
                            new ArrayList<>(Arrays.stream(children).map(f -> f.isDirectory() ? f.getName() + '/' : f.getName()).toList());
                    options.sort(String::compareTo);
                    return new Suggestions(options,
                            line.globalPosition() - line.relativePosition() + prefix.length() + path.length(),
                            line.globalPosition() - line.relativePosition() + line.code().length());
                }
            }
        }
        return null;
    }
}
