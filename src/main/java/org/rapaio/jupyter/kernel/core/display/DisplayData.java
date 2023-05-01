package org.rapaio.jupyter.kernel.core.display;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;


/**
 * Display data structure described in:
 * <a href="https://jupyter-client.readthedocs.io/en/stable/messaging.html#display-data">display-data</a>
 */
public class DisplayData {

    public static DisplayData withText(String text) {
        DisplayData dd = new DisplayData();
        dd.putText(text);
        return dd;
    }

    public static DisplayData withHtml(String html) {
        DisplayData dd = new DisplayData();
        dd.putHTML(html);
        return dd;
    }

    public static DisplayData emptyIfNull(DisplayData displayData) {
        return displayData == null ? new DisplayData() : displayData;
    }

    public static final String DISPLAY_ID_KEY = "display_id";

    @SerializedName("data")
    protected final Map<String, Object> data;

    @SerializedName("metadata")
    protected final Map<String, Object> metadata;

    @SerializedName("transient")
    protected final Map<String, Object> transientData;

    protected DisplayData() {
        this(new HashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    protected DisplayData(Map<String, Object> data, Map<String, Object> metadata, Map<String, Object> transientData) {
        this.data = new HashMap<>(data);
        this.metadata = new LinkedHashMap<>(metadata);
        this.transientData = new LinkedHashMap<>(transientData);
    }

    public DisplayData copy() {
        DisplayData copy = new DisplayData();
        copy.data.putAll(data);
        copy.metadata.putAll(metadata);
        copy.transientData.putAll(transientData);
        return copy;
    }

    public Map<String, Object> data() {
        return data;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }

    public Map<String, Object> transientData() {
        return transientData;
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
     */
    public void putTransient(String key, Object value) {
        transientData.put(key, value);
    }

    public void setDisplayId(String id) {
        putTransient(DISPLAY_ID_KEY, id);
    }

    public boolean hasDisplayId() {
        return transientData.containsKey(DISPLAY_ID_KEY);
    }

    public String getDisplayId() {
        Object id = transientData.get(DISPLAY_ID_KEY);
        return id == null ? null : String.valueOf(id);
    }

    public void putText(String text) {
        putData(MIMEType.TEXT, text);
    }

    public void putHTML(String html) {
        putData(MIMEType.HTML, html);
    }

    public void putData(MIMEType mimeType, String data) {
        putData(mimeType.toString(), data);
    }
}
