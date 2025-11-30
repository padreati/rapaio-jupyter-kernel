package org.rapaio.jupyter.kernel.core.magic.interpolate;

public class InterpolationException extends Exception {

    private final int position;
    private final int length;

    public InterpolationException(String message, int position, int length) {
        super(message);
        this.position = position;
        this.length = length;
    }

    public InterpolationException(String message, int position, int length, Throwable cause) {
        super(message, cause);
        this.position = position;
        this.length = length;
    }

    public int position() {
        return position;
    }

    public int length() {
        return length;
    }
}
