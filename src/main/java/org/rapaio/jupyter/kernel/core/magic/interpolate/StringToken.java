package org.rapaio.jupyter.kernel.core.magic.interpolate;

import java.util.Objects;

public class StringToken {

    public static StringToken interpolate(String value, int pos) {
        return new StringToken(value, pos, true);
    }

    public static StringToken text(String value, int pos) {
        return new StringToken(value, pos, false);
    }

    private final String originalValue;
    private final int originalPosition;
    private final boolean interpolate;

    private StringToken(String originalValue, int originalPosition, boolean interpolate) {
        this.originalValue = originalValue;
        this.originalPosition = originalPosition;
        this.interpolate = interpolate;
    }

    public String originalValue() {
        return originalValue;
    }

    public int originalPosition() {
        return originalPosition;
    }

    public String innerValue() {
        if (!interpolate) {
            return originalValue;
        }
        return originalValue.substring(2, originalValue.length() - 1);
    }

    public boolean canInterpolate() {
        return interpolate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StringToken that)) {
            return false;
        }
        return interpolate == that.interpolate && Objects.equals(originalValue, that.originalValue)
                && originalPosition == that.originalPosition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalValue, originalPosition, interpolate);
    }

    @Override
    public String toString() {
        return "StringToken{" +
                "originalValue='" + originalValue + '\'' +
                ", originalPosition=" + originalPosition +
                ", interpolate=" + interpolate +
                '}';
    }
}
