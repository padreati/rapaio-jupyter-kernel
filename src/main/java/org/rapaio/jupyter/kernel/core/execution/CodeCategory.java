package org.rapaio.jupyter.kernel.core.execution;

public final class CodeCategory {

    public enum Type {
        MAGIC,
        JAVA
    }

    public static Type findType(String source) {
        if(source.startsWith("%")) {
            return Type.MAGIC;
        }
        return Type.JAVA;
    }
}
