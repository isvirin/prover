package io.prover.provermvp.transport;

import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by babay on 15.11.2017.
 */

public class BufferHolder {
    public final AtomicInteger bufferCounter2 = new AtomicInteger();
    private final AtomicInteger bufferCounter = new AtomicInteger();
    volatile boolean recording;
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
        if (recording) {
            int i = 0;
            i++;
        }
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

    public void onBufferAddedToCamera() {
        bufferCounter2.incrementAndGet();
        Log.d("TAG", "buffer   added to camera; total: " + bufferCounter);
    }

    public void onBufferGotFromCamera() {
        bufferCounter2.decrementAndGet();
        Log.d("TAG", "buf received from camera; total: " + bufferCounter);
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public interface OnBufferReleasedListener {
        boolean onBufferReleased(byte[] buffer);
    }
}
