package org.rapaio.jupyter.kernel.base.message.messages;

import org.rapaio.jupyter.kernel.base.message.ContentType;
import org.rapaio.jupyter.kernel.base.message.MessageType;

import com.google.gson.annotations.SerializedName;

public class ShellHistoryRequest implements ContentType<ShellHistoryRequest> {

    @Override
    public MessageType<ShellHistoryRequest> type() {
        return MessageType.SHELL_HISTORY_REQUEST;
    }

    public enum AccessType {
        @SerializedName("range") RANGE,
        @SerializedName("tail") TAIL,
        @SerializedName("search") SEARCH,
    }

    /**
     * If true, include the output associated with the inputs.
     */
    protected final boolean output;

    /**
     * If true, return the raw input history, else the transformed input.
     */
    protected final boolean raw;

    @SerializedName("hist_access_type")
    protected final AccessType accessType;

    private ShellHistoryRequest(boolean output, boolean raw, AccessType accessType) {
        this.output = output;
        this.raw = raw;
        this.accessType = accessType;
    }

    public boolean includeOutput() {
        return output;
    }

    public boolean useRaw() {
        return raw;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public static class Range extends ShellHistoryRequest {
        /**
         * A session index that counts up each time the kernel
         * starts. If negative the number is counting back from
         * the current session.
         */
        protected final int session;

        /**
         * Start cell (execution count number) within the session.
         */
        protected final int start;

        /**
         * Stop cell (execution count number) with the session.
         */
        protected final int stop;

        public Range(boolean output, boolean raw, int session, int start, int stop) {
            super(output, raw, AccessType.RANGE);
            this.session = session;
            this.start = start;
            this.stop = stop;
        }

        public int getSessionIndex() {
            return session;
        }

        public int getStart() {
            return start;
        }

        public int getStop() {
            return stop;
        }
    }

    public static class Tail extends ShellHistoryRequest {
        /**
         * Get the last n executions
         */
        protected final int n;

        public Tail(boolean output, boolean raw, int n) {
            super(output, raw, AccessType.TAIL);
            this.n = n;
        }

        public int getMaxReturnLength() {
            return n;
        }
    }

    public static class Search extends ShellHistoryRequest {
        /**
         * Get the last n executions
         */
        protected final int n;

        /**
         * Glob primary filter with '*' and '?'. Default to '*'
         */
        protected final String pattern;

        /**
         * If true, omit duplicate entries in the return. Defaults
         * to false.
         */
        protected final boolean unique;

        public Search(boolean output, boolean raw, int n, String pattern, boolean unique) {
            super(output, raw, AccessType.SEARCH);
            this.n = n;
            this.pattern = pattern;
            this.unique = unique;
        }

        public int getMaxReturnLength() {
            return n;
        }

        public String getPattern() {
            return pattern;
        }

        public boolean filterUnique() {
            return unique;
        }
    }
}
