package org.rapaio.jupyter.kernel.core.magic.maven;

import org.apache.ivy.core.module.id.ModuleRevisionId;

public record DepCoordinates(String groupId, String artifactId, String version) {

    public DepCoordinates(String identifier) {
        this(identifier.substring(0, identifier.indexOf(':')),
                identifier.substring(identifier.indexOf(':') + 1, identifier.lastIndexOf(':')),
                identifier.substring(identifier.lastIndexOf(':') + 1));
    }

    public ModuleRevisionId revisionId() {
        return ModuleRevisionId.newInstance(groupId, artifactId, version);
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
