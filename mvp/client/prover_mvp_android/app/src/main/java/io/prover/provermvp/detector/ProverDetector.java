package io.prover.provermvp.detector;

import android.media.Image;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Locale;

import io.prover.provermvp.BuildConfig;
import io.prover.provermvp.Settings;
import io.prover.provermvp.camera.Size;
import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.util.Frame;
import io.prover.provermvp.util.FrameRateCounter;

import static io.prover.provermvp.Const.TAG;
import static io.prover.provermvp.detector.DetectionState.State.Confirmed;
import static io.prover.provermvp.detector.DetectionState.State.InputCode;
import static io.prover.provermvp.detector.DetectionState.State.Waiting;

/**
 * Created by babay on 11.11.2017.
 */

public class ProverDetector {
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    private final int[] detectionResult = new int[5];
    private final CameraController cameraController;
    private final FrameRateCounter fpsCounter = new FrameRateCounter(60, 3);
    private final DetectorTimesCounter timesCounter = new DetectorTimesCounter(120);
    long detectionTimeSum = 0;
    int detectionCalls = 0;
    private DetectionState detectionState;
    private int orientationHint;
    private boolean swypeCodeConfirmed = false;
    private long nativeHandler;
    private String swypeCode;

    public ProverDetector(CameraController cameraController) {
        this.cameraController = cameraController;
    }

    public void init(Size videoSize, Size detectorSize, int orientation) {
        this.orientationHint = orientation;
        if (nativeHandler == 0) {
            nativeHandler = initSwype(videoSize.ratio, detectorSize.width, detectorSize.height);
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

        setSwype(nativeHandler, swypeCode);
        if (sendNotification)
            cameraController.notifyActualSwypeCodeSet(swypeCode);
    }

    public synchronized void release() {
        if (nativeHandler != 0) {
            releaseNativeHandler(nativeHandler);
        }
        nativeHandler = 0;
        Log.d(TAG, String.format("detect3 average detect time: %f", detectionTimeSum / (double) detectionCalls));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void detectFrame(Frame frame) {
        int width = frame.width;
        int height = frame.height;

        if (nativeHandler != 0) {
            long time = System.currentTimeMillis();
            if (frame.image != null) {
                Image.Plane[] planes = frame.image.getPlanes();
                detectFrameY_8Buf(nativeHandler, planes[0].getBuffer(), width, height, frame.timeStamp, detectionResult);
            } else if (frame.data != null) {
                detectFrameNV21(nativeHandler, frame.data, width, height, detectionResult);
            }

            timesCounter.add(System.currentTimeMillis() - time);
            detectionTimeSum += (System.currentTimeMillis() - time);
            detectionCalls++;
            if (BuildConfig.DEBUG)
                Log.d(TAG, "detection took: " + (System.currentTimeMillis() - time));
            if (Settings.ENABLE_SCREEN_LOG) {
                String text = String.format(Locale.getDefault(), "Detector: %dx%d, f%d %d,%d,%d,%d,%d, %d ms",
                        width, height, frame.format,
                        detectionResult[0], detectionResult[1], detectionResult[2], detectionResult[3], detectionResult[4],
                        System.currentTimeMillis() - time);
                cameraController.addToScreenLog(text);
            }
        }
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
            if (detectionState != null && detectionState.state == Confirmed && !swypeCodeConfirmed) {
                cameraController.onSwypeCodeConfirmed();
                swypeCodeConfirmed = true;
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
     *
     * @param videoAspectRatio
     * @param detectorWidth
     *@param detectorHeight
     */
    private native long initSwype(float videoAspectRatio, int detectorWidth, int detectorHeight);

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

    private native long detectFrameY_8Buf(long nativeHandler, ByteBuffer planeY, int width, int height, int timestamp, int[] result);

    private native void releaseNativeHandler(long nativeHandler);
}
