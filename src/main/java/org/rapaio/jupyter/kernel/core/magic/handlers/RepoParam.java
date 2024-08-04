package org.rapaio.jupyter.kernel.core.magic.handlers;

public record RepoParam(boolean release, String releaseUpdate, boolean snapshot, String snapshotUpdate) {

    public static RepoParam defaultLocal() {
        return new RepoParam(true, "never", true, "never");
    }

    public static RepoParam defaultRemote() {
        return new RepoParam(true, "never", false, "never");
    }
}
