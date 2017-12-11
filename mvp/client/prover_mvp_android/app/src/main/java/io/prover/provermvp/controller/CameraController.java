package io.prover.provermvp.controller;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.List;

import io.prover.provermvp.camera.Size;
import io.prover.provermvp.detector.DetectionState;
import io.prover.provermvp.detector.SwypeDetectorHandler;
import io.prover.provermvp.util.Frame;
import io.prover.provermvp.util.FrameRateCounter;
import io.prover.provermvp.viewholder.SwypeStateHelperHolder;

/**
 * Created by babay on 17.11.2017.
 */

public class CameraController extends CameraControllerBase {

    private final FrameRateCounter fpsCounter = new FrameRateCounter(60, 10);
    private boolean recording;
    private SwypeStateHelperHolder swypeStateHelperHolder;
    private volatile float detectorFps;
    private int orientationHint;
    private String swypeCode;
    private String actualSwypeCode;
    private long videoStartTime;
    private SwypeDetectorHandler swypeDetectorHandler;

    public CameraController() {
    }

    public boolean isRecording() {
        return recording;
    }

    public void onRecordingStart(float averageFps, Size detectorSize, Size videoSize, int orientationHint) {
        recording = true;
        this.orientationHint = orientationHint;
        onRecordingStart.postNotifyEvent(averageFps, detectorSize);
        videoStartTime = System.currentTimeMillis();
        swypeDetectorHandler = SwypeDetectorHandler.newHandler((int) averageFps, swypeCode, this);
    }

    public void beforeRecordingStop() {
        SwypeDetectorHandler sdh = swypeDetectorHandler;
        if (sdh != null) {
            sdh.quitSync();
            swypeDetectorHandler = null;
        }
    }

    public void onRecordingStop(File file) {
        recording = false;
        boolean isVideoConfirmed = swypeStateHelperHolder.isVideoConfirmed();
        actualSwypeCode = swypeCode = null;
        onRecordingStop.postNotifyEvent(file, isVideoConfirmed);
    }

    public void setSwypeStateHelperHolder(SwypeStateHelperHolder swypeStateHelperHolder) {
        this.swypeStateHelperHolder = swypeStateHelperHolder;
    }

    public void notifyDetectionStateChanged(@Nullable DetectionState oldState, @NonNull DetectionState newState) {
        detectionState.postNotifyEvent(oldState, newState);
    }

    public void setSwypeCode(String swypeCode) {
        this.swypeCode = swypeCode;
        actualSwypeCode = null;
        swypeCodeSet.postNotifyEvent(swypeCode, actualSwypeCode);
    }

    public void onDetectorFpsUpdate(float fps) {
        detectorFps = fps;
    }

    public int getOrientationHint() {
        return orientationHint;
    }

    public void notifyActualSwypeCodeSet(String swypeCode) {
        this.actualSwypeCode = swypeCode;
        swypeCodeSet.postNotifyEvent(this.swypeCode, actualSwypeCode);
    }

    public void onFrameDone() {
        float result = fpsCounter.addFrame();
        if (result >= 0) {
            fpsUpdateListener.postNotifyEvent(result, detectorFps);
        }
    }

    public float getAvgFps() {
        return fpsCounter.getAvgFps();
    }

    public void onFrameAvailable(Frame frame) {
        SwypeDetectorHandler sdh = swypeDetectorHandler;
        if (sdh == null) {
            frame.recycle();
        } else if (sdh.isAlive()) {
            frame.setTimeStamp((int) (System.currentTimeMillis() - videoStartTime));
            sdh.onFrameAvailable(frame);
        } else {
            swypeDetectorHandler = null;
        }
    }

    public void onPreviewStart(List<Size> cameraResolutions, Size mVideoSize) {
        previewStart.postNotifyEvent(cameraResolutions, mVideoSize);
        videoStartTime = System.currentTimeMillis();
    }

    public void onSwypeCodeConfirmed() {
        swypeCodeConfirmed.postNotifyEvent();
    }

}
