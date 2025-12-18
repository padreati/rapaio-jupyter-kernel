package org.rapaio.jupyter.kernel.global;

import java.util.HashMap;
import java.util.Map;

import org.rapaio.jupyter.kernel.display.MimeType;

public class Config {

    private static final String DEFAULT_MIME = "display.defaultMime";
    private static final String DEFAULT_MIME_VALUE = MimeType.HTML.longType();

    private static final String DEFAULT_MIME_IMAGE = "display.defaultMimeImage";
    private static final String DEFAULT_MIME_IMAGE_VALUE = MimeType.PNG.longType();

    private final HashMap<String, String> valueMap = new HashMap<>();

    public Config() {
        valueMap.put(DEFAULT_MIME, DEFAULT_MIME_VALUE);
        valueMap.put(DEFAULT_MIME_IMAGE, DEFAULT_MIME_IMAGE_VALUE);
    }

    public String get(String key) {
        return valueMap.get(key);
    }

    public void set(String key, String value) {
        valueMap.put(key, value);
    }

    public Map<String, String> getMapValues() {
        return new HashMap<>(valueMap);
    }

    public DisplayPreferences display() {
        return new DisplayPreferences(this);
    }

    public static class DisplayPreferences {

        private final Config config;

        private DisplayPreferences(Config config) {
            this.config = config;
        }

        public String defaultMime() {
            return config.get(DEFAULT_MIME);
        }

        public void defaultMime(String value) {
            config.set(DEFAULT_MIME, value);
        }

        public String defaultMimeImage() {
            return config.get(DEFAULT_MIME_IMAGE);
        }

        public void defaultMimeImage(String value) {
            config.set(DEFAULT_MIME_IMAGE, value);
        }
    }
}
