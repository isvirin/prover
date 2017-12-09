package io.prover.provermvp.detector;

import android.graphics.ImageFormat;
import android.media.Image;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.nio.ByteBuffer;

import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.util.Frame;
import io.prover.provermvp.util.FrameRateCounter;

import static io.prover.provermvp.Const.TAG;
import static io.prover.provermvp.detector.DetectionState.State.InputCode;
import static io.prover.provermvp.detector.DetectionState.State.Waiting;

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
    int orientationHint;
    private long nativeHandler;
    private volatile boolean isDetectionPaused;
    private String swypeCode;


    public ProverDetector(CameraController cameraController) {
        this.cameraController = cameraController;
        cameraController.swypeDetectionPause.add(this);
    }

    public void init(int fps, int orientation, String swype) {
        this.orientationHint = orientation;
        /*if (orientationHint == 180)
            orientationHint = 0;
        else if (orientation == 0) {
            orientationHint = 180;
        }*/
        initialFps = fps;
        if (swype != null)
            swype = SwypeOrientationHelper.rotateSwypeCode(swype, orientationHint);
        if (nativeHandler == 0) {
            nativeHandler = initSwype(fps, swype);
        }
    }

    public synchronized void setSwype(String swype) {
        String oldSwype = this.swypeCode;
        this.swypeCode = swype == null ? null : SwypeOrientationHelper.rotateSwypeCode(swype, orientationHint);

        updateSwype(swypeCode != null && !swypeCode.equals(oldSwype));
    }

    private void updateSwype(boolean sendNotification) {
        if (nativeHandler == 0) {
            return;
        }

        float avgFrameTime = timesCounter.getAverageTime();
        int fps = avgFrameTime == 0 ? initialFps : (int) (1000 / avgFrameTime);
        fps = Math.min(initialFps, fps);
        Log.d(TAG + "Detector", String.format("initialFps: %d, avgtime: %f, fps: %d", initialFps, avgFrameTime, fps));
        setSwype(nativeHandler, swypeCode, fps);
        if (sendNotification)
            cameraController.notifyActualSwypeCodeSet(swypeCode);
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
    public void detectFrame(Frame frame) {
        if (isDetectionPaused)
            return;
        Image image = frame.image;
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
                detectFrameY_8Buf(nativeHandler, planes[0].getBuffer(), width, height, frame.timeStamp, detectionResult2);
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
            if (oldState != null && oldState.state == InputCode && newState.state == Waiting) {
                updateSwype(true);
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
            if (oldState != null && oldState.state == InputCode && newState.state == Waiting) {
                updateSwype(true);
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

    private native long detectFrameY_8Buf(long nativeHandler, ByteBuffer planeY, int width, int height, int timestamp, long[] result);

    private native void detectFrameNV21Buf(long nativeHandler, ByteBuffer data, int width, int height, int[] result);

    private native void releaseNativeHandler(long nativeHandler);

    @Override
    public void onDetectorPauseChanged(boolean isPaused) {
        //this.isDetectionPaused = isPaused;
        detectionState = null;
    }
}
