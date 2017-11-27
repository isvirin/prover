package io.prover.provermvp.detector;

import android.graphics.ImageFormat;
import android.media.Image;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.nio.ByteBuffer;

import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.util.FrameRateCounter;

import static io.prover.provermvp.Const.TAG;

/**
 * Created by babay on 11.11.2017.
 */

public class ProverDetector implements CameraController.OnDetectorPauseChangedListener {
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    private final int[] detectionResult = new int[5];
    private final long[] detectionResult2 = new long[5];
    private final CameraController cameraController;
    private final FrameRateCounter fpsCounter = new FrameRateCounter(60, 3);
    private final DetectorTimesCounter timesCounter = new DetectorTimesCounter(120);
    DetectionState detectionState;
    int initialFps;
    private long nativeHandler;
    private volatile boolean isDetectionPaused;
    private String swypeCode;


    public ProverDetector(CameraController cameraController) {
        this.cameraController = cameraController;
        cameraController.swypeDetectionPause.add(this);
    }

    public void init(int fps, String swype) {
        initialFps = fps;
        if (nativeHandler == 0) {
            nativeHandler = initSwype(fps, swype);
        }
    }

    public synchronized void setSwype(String swype) {
        this.swypeCode = swype;
        if (nativeHandler == 0) {
            return;
        }

        float avgFrameTime = timesCounter.getAverageTime();
        int fps = avgFrameTime == 0 ? initialFps : (int) (1000 / avgFrameTime);
        fps = Math.min(initialFps, fps);
        Log.d(TAG + "Detector", String.format("initialFps: %d, avgtime: %f, fps: %d", initialFps, avgFrameTime, fps));
        setSwype(nativeHandler, swype, fps);
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
            timesCounter.add(System.currentTimeMillis() - time);
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
        boolean isD2 = false;

        if (nativeHandler != 0) {
            long time = System.currentTimeMillis();
            Image.Plane[] planes = image.getPlanes();

            if (format == ImageFormat.YV12) {
                detectFrameNV21Buf(nativeHandler, planes[0].getBuffer(), width, height, detectionResult);
            } else if (format == ImageFormat.YUV_420_888) {
                isD2 = true;
                //long d = detectFrameYUV420_888Buf2(nativeHandler, planes[0].getBuffer(), planes[1].getBuffer(), planes[2].getBuffer(), width, height, detectionResult2);
                long d = detectFrameY_8Buf(nativeHandler, planes[0].getBuffer(), width, height, detectionResult2);
            }
            timesCounter.add(System.currentTimeMillis() - time);
            Log.d(TAG, "detection took: " + (System.currentTimeMillis() - time));
        }
        if (isD2)
            detectionDone2();
        else
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
        float fps = fpsCounter.addFrame();
        if (fps >= 0) {
            cameraController.onDetectorFpsUpdate(fps);
        }
    }

    private void detectionDone2() {
        if (detectionState == null || !detectionState.isEqualsArray(detectionResult2)) {
            final DetectionState oldState = detectionState;
            final DetectionState newState = new DetectionState(detectionResult2);
            detectionState = newState;
            cameraController.notifyDetectionStateChanged(oldState, newState);
            if (oldState != null && oldState.state == 2 && newState.state == 0) {
                setSwype(swypeCode);
            }
        }
        float fps = fpsCounter.addFrame();
        if (fps >= 0) {
            cameraController.onDetectorFpsUpdate(fps);
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
     * @param fps
     */
    private native void setSwype(long nativeHandler, String swype, int fps);


    /**
     * detect single frame
     *
     * @param frameData
     * @param width
     * @param height
     * @param result    -- an array with 4 items: State, Index, X, Y
     */
    private native void detectFrameNV21(long nativeHandler, byte[] frameData, int width, int height, int[] result);

    private native int detectFrameYUV420_888Buf(long nativeHandler, ByteBuffer planeY, ByteBuffer planeU, ByteBuffer planeV, int width, int height, int[] result);

    private native long detectFrameYUV420_888Buf2(long nativeHandler, ByteBuffer planeY, ByteBuffer planeU, ByteBuffer planeV, int width, int height, long[] result);

    private native long detectFrameY_8Buf(long nativeHandler, ByteBuffer planeY, int width, int height, long[] result);

    private native void detectFrameNV21Buf(long nativeHandler, ByteBuffer data, int width, int height, int[] result);

    private native void releaseNativeHandler(long nativeHandler);

    @Override
    public void onDetectorPauseChanged(boolean isPaused) {
        this.isDetectionPaused = isPaused;
        detectionState = null;
    }
}
