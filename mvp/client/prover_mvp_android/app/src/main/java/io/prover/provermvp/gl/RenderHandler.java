package io.prover.provermvp.gl;

import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

import io.prover.provermvp.camera.Size;

/**
 * {@link Handler} responsible for communication between this render thread and the UI thread.
 * <p>
 * For now, the only thing we really need to worry about is shutting down the thread upon completion
 * of recording, since we cannot access the {@link MediaRecorder} surface once
 * {@link MediaRecorder#stop()} is called.
 */
public class RenderHandler extends Handler {
    private static final String TAG = RenderHandler.class.getSimpleName();

    private static final int MSG_SET_FRAME_SIZE = 0;
    private static final int MSG_SHUTDOWN = 1;

    /**
     * Our camera renderer ref, weak since we're dealing with static class so it doesn't leak
     */
    private WeakReference<ICameraRenderer> mWeakRenderer;

    /**
     * Call from render thread.
     */
    public RenderHandler(ICameraRenderer rt) {
        mWeakRenderer = new WeakReference<>(rt);
    }

    /**
     * Sends the "shutdown" message, which tells the render thread to halt.
     * Call from UI thread.
     */
    public void sendShutdown() {
        sendMessage(obtainMessage(RenderHandler.MSG_SHUTDOWN));
    }

    public void setFrameSize(Size size) {
        sendMessage(obtainMessage(MSG_SET_FRAME_SIZE, size));
    }

    @Override
    public void handleMessage(Message msg) {
        ICameraRenderer renderer = mWeakRenderer.get();
        if (renderer == null) {
            Log.w(TAG, "RenderHandler.handleMessage: weak ref is null");
            return;
        }

        int what = msg.what;
        switch (what) {
            case MSG_SET_FRAME_SIZE:
                Size size = (Size) msg.obj;
                renderer.setFrameSize(size);
                break;

            case MSG_SHUTDOWN:
                renderer.shutdown();
                break;
            default:
                throw new RuntimeException("unknown message " + what);
        }
    }
}
