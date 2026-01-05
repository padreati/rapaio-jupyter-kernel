package org.rapaio.jupyter.kernel.display.html;

import java.util.List;

/**
 * Poor's man html encoder
 */
public class Html {

    private static final List<Pair> list = List.of(
            new Pair("&", "&amp;"),
            new Pair("<", "&lt;"),
            new Pair(">", "&gt;"),
            new Pair("\"", "&quot;"),
            new Pair("'", "&#x27;"));

    public static String encode(String string) {
        String result = string;
        for (Pair pair : list) {
            result = result.replace(pair.key(), pair.value());
        }
        return result;
    }

    record Pair(String key, String value) {
    }
}
