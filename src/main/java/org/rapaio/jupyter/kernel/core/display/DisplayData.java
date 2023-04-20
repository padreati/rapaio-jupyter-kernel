package org.rapaio.jupyter.kernel.core.display;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;


/**
 * Display data structure described in:
 * <a href="https://jupyter-client.readthedocs.io/en/stable/messaging.html#display-data">display-data</a>
 */
public class DisplayData {

    public static final String DISPLAY_ID_KEY = "display_id";

    public static DisplayData emptyIfNull(DisplayData bundle) {
        return bundle == null ? new DisplayData(Collections.emptyMap()) : bundle;
    }

    @SerializedName("data")
    private final Map<String, Object> data = new HashMap<>();

    @SerializedName("metadata")
    private final Map<String, Object> metadata = new LinkedHashMap<>();

    @SerializedName("transient")
    private final Map<String, Object> transientData = new LinkedHashMap<>();

    private DisplayData(Map<String, Object> data) {
        this.data.putAll(data);
    }

    public DisplayData(DisplayData that) {
        this.data.putAll(that.data);
        this.metadata.putAll(that.metadata);
        this.transientData.putAll(that.transientData);
    }

    public DisplayData(String textData) {
        this.putText(textData);
    }

    public DisplayData() {
    }

    public void putData(String mimeType, Object data) {
        this.data.put(mimeType, data);
    }

    public void putMetaData(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * The transient dict contains runtime metadata that should not be persisted to document formats and is fully optional.
     * The only transient key currently defined in Jupyter is display_id.
     *
     * @param key   the data key
     * @param value the data value
     */
    public void putTransient(String key, Object value) {
        transientData.put(key, value);
    }

    public boolean hasDataForKey(String key) {
        return this.data.containsKey(key);
    }

    public void assign(DisplayData dd) {
        data.clear();
        data.putAll(dd.data);
        metadata.clear();
        metadata.putAll(dd.metadata);
        transientData.clear();
        transientData.putAll(dd.transientData);
    }

    public void setDisplayId(String id) {
        this.putTransient(DISPLAY_ID_KEY, id);
    }

    public boolean hasDisplayId() {
        return transientData.containsKey(DISPLAY_ID_KEY);
    }

    public String getDisplayId() {
        Object id = this.transientData.get(DISPLAY_ID_KEY);
        if (id == null) {
            return null;
        }
        return String.valueOf(id);
    }

    public void putText(String text) {
        this.putData("text/plain", text);
    }

    public void putHTML(String html) {
        this.putData("text/html", html);
    }

    public void putLatex(String latex) {
        this.putData("text/latex", latex);
    }

    public void putMath(String math) {
        this.putLatex("$$" + math + "$$");
    }

    public void putMarkdown(String markdown) {
        this.putData("text/markdown", markdown);
    }

    public void putJavaScript(String javascript) {
        this.putData("application/javascript", javascript);
    }

    public void putJSON(String json) {
        this.putData("application/json", json);
    }

    public void putJSON(String json, boolean expanded) {
        this.putJSON(json);
        this.putMetaData("expanded", expanded);
    }

    public void putSVG(String svg) {
        this.putData("image/svg+xml", svg);
    }
}
