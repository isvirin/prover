package io.prover.provermvp.detector;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;

/**
 * Created by babay on 11.11.2017.
 */

public class ProverDetector {
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    private final int[] detectionResult = new int[4];
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final DetectionListener detectionListener;
    DetectionState detectionState;
    private long nativeHandler;

    public ProverDetector(int fps, String swype, DetectionListener detectionListener) {
        this.detectionListener = detectionListener;
        nativeHandler = initSwype(fps, swype);
    }

    public ProverDetector(DetectionListener detectionListener) {
        this.detectionListener = detectionListener;
    }

    public void init(int fps, String swype) {
        if (nativeHandler == 0) {
            nativeHandler = initSwype(fps, swype);
        }
    }

    public synchronized void setSwype(String swype) {
        if (nativeHandler != 0) {
            setSwype(nativeHandler, swype);
        }
    }

    public synchronized void release() {
        if (nativeHandler != 0) {
            releaseNativeHandler(nativeHandler);
        }
        nativeHandler = 0;
    }

    public synchronized void detectFrame(byte[] frameData, int width, int height) {
        if (nativeHandler != 0) {
            Arrays.fill(detectionResult, 0);
            detectFrame(nativeHandler, frameData, width, height, detectionResult);
        }

        if (detectionState == null || !detectionState.isEqualsArray(detectionResult)) {
            final DetectionState oldState = detectionState;
            final DetectionState newState = new DetectionState(detectionResult);
            detectionState = newState;
            handler.post(() -> detectionListener.onDetectionStateChanged(oldState, newState));
        }

    }

    /**
     * initialize swype with specific fps and swype code
     *
     * @param fps
     * @param swype
     * @return
     */
    private native long initSwype(int fps, String swype);

    /**
     * set swype code
     *
     * @param nativeHandler
     * @param swype
     */
    private native void setSwype(long nativeHandler, String swype);


    /**
     * detect single frame
     *
     * @param frameData
     * @param width
     * @param height
     * @param result    -- an array with 4 items: State, Index, X, Y
     */
    private native void detectFrame(long nativeHandler, byte[] frameData, int width, int height, int[] result);

    private native void releaseNativeHandler(long nativeHandler);

    public interface DetectionListener {
        void onDetectionStateChanged(@Nullable DetectionState oldState, @NonNull DetectionState newState);
    }
}
