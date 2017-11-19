package io.prover.provermvp.transport;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by babay on 15.11.2017.
 */

public class BufferHolder {
    public final AtomicInteger bufferCounter2 = new AtomicInteger();
    private final AtomicInteger bufferCounter = new AtomicInteger();
    private int width;
    private int height;
    private int size;
    private OnBufferReleasedListener bufferReleasedListener;

    public void setSize(int width, int height) {
        size = width * height * 3 / 2;
    }

    public byte[] getBuffer() {
        if (size == 0) {
            return null;
        }
        bufferCounter.incrementAndGet();
        return new byte[size];
    }

    public void releaseBuffer(byte[] buffer) {
        if (buffer.length != size || bufferReleasedListener == null)
            return;

        if (!bufferReleasedListener.onBufferReleased(buffer)) {
            bufferCounter.decrementAndGet();
        }
    }

    public void setBufferReleasedListener(OnBufferReleasedListener listener) {
        if (bufferReleasedListener != listener) {
            bufferReleasedListener = listener;
            bufferCounter.set(0);
            bufferCounter2.set(0);
        }
    }

    public void onSetBuffer() {
        bufferCounter2.incrementAndGet();
    }

    public void onGotBuffer() {
        bufferCounter2.decrementAndGet();
    }

    public interface OnBufferReleasedListener {
        boolean onBufferReleased(byte[] buffer);
    }
}
