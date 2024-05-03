package org.rapaio.jupyter.kernel.core.magic.dependencies;

import java.util.HashMap;
import java.util.Map;

import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;

public record Dependency(String organization, String name, String revision, String ext, String classifier, boolean force) {

    public static Dependency from(String identifier, boolean force) {
        String[] token = identifier.split("[:#;]");
        if (token.length == 3) {
            return new Dependency(token[0], token[1], token[2], null, null, force);
        }
        if (token.length == 4) {
            return new Dependency(token[0], token[1], token[2], token[3], null, force);
        }
        if (token.length == 5) {
            return new Dependency(token[0], token[1], token[2], token[3], token[4], force);
        }
        throw new RuntimeException("Unknown dependency identifier: " + identifier);
    }

    public ModuleRevisionId revisionId() {
        Map<String, String> extraAttributes = new HashMap<>();
        if (ext != null && !ext.isEmpty()) {
            extraAttributes.put("ext", ext);
        }
        if (classifier != null && !classifier.isEmpty()) {
            extraAttributes.put("classifier", classifier);
        }
        return ModuleRevisionId.newInstance(organization, name, revision, extraAttributes);
    }

    public ModuleId moduleId() {
        return ModuleId.newInstance(organization, name);
    }

    @Override
    public String toString() {
        return organization + ":" + name + ":" + revision;
    }
}
