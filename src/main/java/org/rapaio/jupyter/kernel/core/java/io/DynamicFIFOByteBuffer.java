package org.rapaio.jupyter.kernel.core.java.io;

/**
 * A dynamic byte buffer. It allows appending data at the end and consuming bytes from beginning.
 * At each operation it tests if the space can be saved or changed, depending on situations.
 * <p>
 * It is basically like a circular byte buffer who's size is dynamic.
 */
public class DynamicFIFOByteBuffer {

    private byte[] buffer = new byte[1024];
    private int start;
    private int end;

    public DynamicFIFOByteBuffer() {
    }

    public boolean canTake(int n) {
        return end - start >= n;
    }

    public int availableToTake() {
        return start - end;
    }

    public byte[] take(int n) {
        if (n > end - start) {
            throw new IllegalArgumentException("Not enough data to take.");
        }
        byte[] data = new byte[n];
        System.arraycopy(buffer, start, data, 0, n);
        start += n;
        return data;
    }

    public void feed(byte[] data, int len) {
        if (start == 0) {
            // we are at the start of the buffer
            if (end + len < buffer.length) {
                // we have room for input
                System.arraycopy(data, 0, buffer, 0, len);
            } else {
                // don't have room for input, we allocate
                byte[] copy = new byte[Math.max(end + len, (int) (buffer.length * 1.5))];
                System.arraycopy(buffer, 0, copy, 0, end);
                System.arraycopy(data, 0, copy, end, len);
                buffer = copy;
            }
            end += len;
        } else {
            // we are not at the start of the buffer
            if (end + len < buffer.length) {
                // we can hold additional input with moving the front part
                if (end - start + len < start) {
                    System.arraycopy(buffer, start, buffer, 0, end - start);
                    System.arraycopy(data, 0, buffer, end - start, data.length);
                    end = end - start + data.length;
                    start = 0;
                } else {
                    // we can hold the new input, but the front part is small
                    System.arraycopy(data, 0, buffer, end, data.length);
                    end += data.length;
                }
            } else {
                // no room, new buffer anyway
                byte[] copy = new byte[Math.max(end + len, (int) (buffer.length * 1.5))];
                System.arraycopy(buffer, start, buffer, 0, end - start);
                System.arraycopy(data, 0, buffer, end - start, data.length);
                buffer = copy;
                end = end - start + len;
                start = 0;
            }
        }
    }
}
