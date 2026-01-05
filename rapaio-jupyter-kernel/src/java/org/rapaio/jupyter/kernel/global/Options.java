package org.rapaio.jupyter.kernel.global;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class Options {

    private static final String DEFAULT_MIME = "display.defaultMime";
    private static final String DEFAULT_MIME_IMAGE = "display.defaultMimeImage";
    private static final String MAX_SEQ_ITEMS = "display.maxSeqItems";
    private static final String MAX_ROWS_KEY = "display.maxRows";
    private static final String MAX_COLS_KEY = "display.maxCols";
    private static final String MAX_COL_WIDTH_KEY = "display.maxColWidth";
    private static final String DISPLAY_MAX_COL_WIDTH_KEY = "display.showIndex";
    private static final String DISPLAY_FORMAT_NA_KEY = "display.format.na";
    private static final String DISPLAY_FORMAT_PRECISION_KEY = "DISPLAY_FORMAT_PRECISION_KEY";
    private static final String DISPLAY_HTML_BORDER_KEY = "display.html.border";

    private final HashMap<String, Item> valueMap = new HashMap<>();

    public record Item(String key, String value, String description) {
    }

    public Options() {
        try {
            Gson gson = new Gson();
            var reader = gson.newJsonReader(new BufferedReader(new InputStreamReader(
                    Options.class.getClassLoader().getResourceAsStream("options.json"))));
            Item[] configItems = gson.fromJson(reader, Item[].class);
            for (var config : configItems) {
                valueMap.put(config.key, config);
            }
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String get(String key) {
        return valueMap.get(key).value;
    }

    public String getDescription(String key) {
        return valueMap.get(key).description;
    }

    public void set(String key, String value) {
        set(key, value, null);
    }

    public void set(String key, String value, String description) {
        Item configItem = new Item(key, value,
                description == null && valueMap.containsKey(key) ? valueMap.get(key).description : description);
        valueMap.put(key, configItem);
    }

    public List<Item> getConfigItems() {
        return new ArrayList<>(valueMap.values());
    }

    public OptionsDisplay display() {
        return new OptionsDisplay(this);
    }

    public static class OptionsDisplay {

        private final Options options;

        private OptionsDisplay(Options options) {
            this.options = options;
        }

        public String defaultMime() {
            return options.get(DEFAULT_MIME);
        }

        public void defaultMime(String value) {
            options.set(DEFAULT_MIME, value);
        }

        public String defaultMimeImage() {
            return options.get(DEFAULT_MIME_IMAGE);
        }

        public void defaultMimeImage(String value) {
            options.set(DEFAULT_MIME_IMAGE, value);
        }

        public int maxSeqItems() {
            return Integer.parseInt(options.get(MAX_SEQ_ITEMS));
        }

        public void maxSeqItems(int value) {
            options.set(MAX_SEQ_ITEMS, Integer.toString(value));
        }

        public int maxRows() {
            return Integer.parseInt(options.get(MAX_ROWS_KEY));
        }

        public void maxRows(int value) {
            options.set(MAX_ROWS_KEY, Integer.toString(value));
        }

        public int maxCols() {
            return Integer.parseInt(options.get(MAX_COLS_KEY));
        }

        public void maxCols(int value) {
            options.set(MAX_COLS_KEY, Integer.toString(value));
        }

        public int maxColWidth() {
            return Integer.parseInt(options.get(MAX_COL_WIDTH_KEY));
        }

        public void maxColWidth(int value) {
            options.set(MAX_COL_WIDTH_KEY, Integer.toString(value));
        }

        public boolean showIndex() {
            return options.get(DISPLAY_MAX_COL_WIDTH_KEY).equalsIgnoreCase("true");
        }

        public void showIndex(boolean value) {
            options.set(DISPLAY_MAX_COL_WIDTH_KEY, value ? "true" : "false");
        }

        public OptionsDisplayFormat format() {
            return new OptionsDisplayFormat(this.options);
        }

        public OptionsDisplayHtml html() {
            return new OptionsDisplayHtml(this.options);
        }
    }

    public static class OptionsDisplayFormat {

        private final Options options;

        private OptionsDisplayFormat(Options options) {
            this.options = options;
        }

        public String na() {
            return options.get(DISPLAY_FORMAT_NA_KEY);
        }

        public void na(String value) {
            options.set(DISPLAY_FORMAT_NA_KEY, value);
        }

        public int precision() {
            return Integer.parseInt(options.get(DISPLAY_FORMAT_PRECISION_KEY));
        }

        public void precision(int precision) {
            options.set(DISPLAY_FORMAT_PRECISION_KEY, String.valueOf(precision));
        }
    }

    public static class OptionsDisplayHtml {

        private final Options options;

        private OptionsDisplayHtml(Options options) {
            this.options = options;
        }

        public int border() {
            return Integer.parseInt(options.get(DISPLAY_HTML_BORDER_KEY));
        }

        public void border(int value) {
            options.set(DISPLAY_HTML_BORDER_KEY, String.valueOf(value));
        }
    }
}
