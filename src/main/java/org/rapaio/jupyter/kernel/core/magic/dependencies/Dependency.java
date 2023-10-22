package org.rapaio.jupyter.kernel.core.magic.dependencies;

import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;

public record Dependency(String groupId, String artifactId, String version, boolean force) {

    public Dependency(String identifier, boolean force) {
        this(
                identifier.substring(0, identifier.indexOf(':')),
                identifier.substring(identifier.indexOf(':') + 1, identifier.lastIndexOf(':')),
                identifier.substring(identifier.lastIndexOf(':') + 1),
                force
        );
    }

    public ModuleRevisionId revisionId() {
        return ModuleRevisionId.newInstance(groupId, artifactId, version);
    }

    public ModuleId moduleId() {
        return ModuleId.newInstance(groupId, artifactId);
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
