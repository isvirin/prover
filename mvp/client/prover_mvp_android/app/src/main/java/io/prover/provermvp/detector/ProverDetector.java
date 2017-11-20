package io.prover.provermvp.detector;

import android.graphics.ImageFormat;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.nio.ByteBuffer;

import io.prover.provermvp.controller.CameraController;

import static io.prover.provermvp.Const.TAG;

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
    private final CameraController cameraController;
    DetectionState detectionState;
    private long nativeHandler;


    public ProverDetector(DetectionListener detectionListener, CameraController cameraController) {
        this.detectionListener = detectionListener;
        this.cameraController = cameraController;
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

    public void detectFrame(byte[] frameData, int width, int height) {
        if (nativeHandler != 0) {
            long time = System.currentTimeMillis();
            detectFrameNV21(nativeHandler, frameData, width, height, detectionResult);
            Log.d(TAG, "detection took: " + (System.currentTimeMillis() - time));
        }

        if (detectionState == null || !detectionState.isEqualsArray(detectionResult)) {
            final DetectionState oldState = detectionState;
            final DetectionState newState = new DetectionState(detectionResult);
            detectionState = newState;
            handler.post(() -> detectionListener.onDetectionStateChanged(oldState, newState));
        }

        cameraController.frameReleased.postNotifyEvent(frameData);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void detectFrame(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int format = image.getFormat();

        if (nativeHandler != 0) {
            long time = System.currentTimeMillis();
            Image.Plane[] planes = image.getPlanes();

            if (format == ImageFormat.YV12) {
                detectFrameNV21Buf(nativeHandler, planes[0].getBuffer(), width, height, detectionResult);
            } else if (format == ImageFormat.YUV_420_888) {
                detectFrameYUV420_888Buf(nativeHandler, planes[0].getBuffer(), planes[1].getBuffer(), planes[2].getBuffer(), width, height, detectionResult);
            }

            Log.d(TAG, "detection took: " + (System.currentTimeMillis() - time));
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
    private native void detectFrameNV21(long nativeHandler, byte[] frameData, int width, int height, int[] result);

    private native void detectFrameYUV420_888Buf(long nativeHandler, ByteBuffer planeY, ByteBuffer planeU, ByteBuffer planeV, int width, int height, int[] result);

    private native void detectFrameNV21Buf(long nativeHandler, ByteBuffer data, int width, int height, int[] result);

    private native void releaseNativeHandler(long nativeHandler);


    public interface DetectionListener {
        void onDetectionStateChanged(@Nullable DetectionState oldState, @NonNull DetectionState newState);
    }
}
