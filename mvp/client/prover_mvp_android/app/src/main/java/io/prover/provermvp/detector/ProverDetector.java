package io.prover.provermvp.detector;

import android.graphics.ImageFormat;
import android.media.Image;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.nio.ByteBuffer;

import io.prover.provermvp.controller.CameraController;

import static io.prover.provermvp.Const.TAG;

/**
 * Created by babay on 11.11.2017.
 */

public class ProverDetector implements CameraController.OnDetectorPauseChangedListener {
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    private final int[] detectionResult = new int[4];
    private final CameraController cameraController;
    DetectionState detectionState;
    private long nativeHandler;
    private volatile boolean isDetectionPaused;
    private String swypeCode;


    public ProverDetector(CameraController cameraController) {
        this.cameraController = cameraController;
        cameraController.swypeDetectionPause.add(this);
    }

    public void init(int fps, String swype) {
        if (nativeHandler == 0) {
            nativeHandler = initSwype(fps, swype);
        }
    }

    public synchronized void setSwype(String swype) {
        this.swypeCode = swype;
        if (nativeHandler != 0) {
            setSwype(nativeHandler, swype);
        }
    }

    public synchronized void release() {
        if (nativeHandler != 0) {
            releaseNativeHandler(nativeHandler);
        }
        nativeHandler = 0;
        cameraController.swypeDetectionPause.remove(this);
    }

    public void detectFrame(byte[] frameData, int width, int height) {
        if (isDetectionPaused)
            return;
        if (nativeHandler != 0) {
            long time = System.currentTimeMillis();
            detectFrameNV21(nativeHandler, frameData, width, height, detectionResult);
            Log.d(TAG, "detection took: " + (System.currentTimeMillis() - time));
        }
        cameraController.frameReleased.postNotifyEvent(frameData);
        detectionDone();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void detectFrame(Image image) {
        if (isDetectionPaused)
            return;
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
        detectionDone();
    }

    private void detectionDone() {
        if (detectionState == null || !detectionState.isEqualsArray(detectionResult)) {
            final DetectionState oldState = detectionState;
            final DetectionState newState = new DetectionState(detectionResult);
            detectionState = newState;
            cameraController.notifyDetectionStateChanged(oldState, newState);
            if (oldState != null && oldState.state == 2 && newState.state == 0) {
                setSwype(swypeCode);
            }
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

    @Override
    public void onDetectorPauseChanged(boolean isPaused) {
        this.isDetectionPaused = isPaused;
        detectionState = null;
    }
}
