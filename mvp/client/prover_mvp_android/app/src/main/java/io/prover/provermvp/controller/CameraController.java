package io.prover.provermvp.controller;

import android.hardware.Camera;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.List;

import io.prover.provermvp.camera.Size;
import io.prover.provermvp.detector.DetectionState;
import io.prover.provermvp.transport.NetworkRequest;
import io.prover.provermvp.viewholder.SwypeStateHelperHolder;

/**
 * Created by babay on 17.11.2017.
 */

public class CameraController {

    public final Handler handler = new Handler(Looper.getMainLooper());

    public final ListenerList2<OnPreviewStartListener, List<Size>, Size> previewStart
            = new ListenerList2<>(handler, OnPreviewStartListener::onPreviewStart);

    public final ListenerList2<OnFrameAvailableListener, byte[], Camera> frameAvailable
            = new ListenerList2<>(handler, OnFrameAvailableListener::onFrameAvailable);

    public final ListenerList1<OnFrameReleasedListener, byte[]> frameReleased
            = new ListenerList1<>(handler, OnFrameReleasedListener::onFrameReleased);

    public final ListenerList1<OnFrameAvailable2Listener, Image> frameAvailable2
            = new ListenerList1<>(handler, OnFrameAvailable2Listener::onFrameAvailable);

    public final ListenerList2<OnRecordingStartListener, Float, Size> onRecordingStart
            = new ListenerList2<>(handler, OnRecordingStartListener::onRecordingStart);

    public final ListenerList2<OnRecordingStopListener, File, Boolean> onRecordingStop
            = new ListenerList2<>(handler, OnRecordingStopListener::onRecordingStop);

    public final ListenerList1<NetworkRequestStartListener, NetworkRequest> onNetworkRequestStart
            = new ListenerList1<>(handler, NetworkRequestStartListener::onNetworkRequestStart);

    public final ListenerList2<NetworkRequestDoneListener, NetworkRequest, Object> onNetworkRequestDone
            = new ListenerList2<>(handler, NetworkRequestDoneListener::onNetworkRequestDone);

    public final ListenerList2<NetworkRequestErrorListener, NetworkRequest, Exception> onNetworkRequestError
            = new ListenerList2<>(handler, NetworkRequestErrorListener::onNetworkRequestError);

    public final ListenerList2<OnDetectionStateCahngedListener, DetectionState, DetectionState> detectionState
            = new ListenerList2<>(handler, OnDetectionStateCahngedListener::onDetectionStateChanged);

    public final ListenerList1<OnSwypeCodeSetListener, String> swypeCodeSet
            = new ListenerList1<>(handler, OnSwypeCodeSetListener::onSwypeCodeSet);

    public final ListenerList1<OnDetectorPauseChangedListener, Boolean> swypeDetectionPause
            = new ListenerList1<>(handler, OnDetectorPauseChangedListener::onDetectorPauseChanged);

    public final NetworkDelegate networkDelegate = new NetworkDelegate();
    private boolean recording;
    private SwypeStateHelperHolder swypeStateHelperHolder;

    private volatile float detectorFps;
    private int orientationHint;

    public CameraController() {
    }

    public boolean isRecording() {
        return recording;
    }

    public void onRecordingStart(float averageFps, Size detectorSize, int orientationHint) {
        recording = true;
        this.orientationHint = orientationHint;
        onRecordingStart.postNotifyEvent(averageFps, detectorSize);
    }

    public void onRecordingStop(File file) {
        recording = false;
        boolean isVideoConfirmed = swypeStateHelperHolder.isVideoConfirmed();
        onRecordingStop.postNotifyEvent(file, isVideoConfirmed);
    }

    public void setSwypeStateHelperHolder(SwypeStateHelperHolder swypeStateHelperHolder) {
        this.swypeStateHelperHolder = swypeStateHelperHolder;
    }

    public void notifyDetectionStateChanged(@Nullable DetectionState oldState, @NonNull DetectionState newState) {
        detectionState.postNotifyEvent(oldState, newState);
    }

    public void setSwypeCode(String swypeCode) {
        swypeCodeSet.postNotifyEvent(swypeCode);
    }

    public void setSwypeDetectorPaused(boolean paused) {
        swypeDetectionPause.notifyEvent(paused);
    }

    public void onDetectorFpsUpdate(float fps) {
        detectorFps = fps;
    }

    public float getDetectorFps() {
        return detectorFps;
    }

    public int getOrientationHint() {
        return orientationHint;
    }

    public interface OnPreviewStartListener {
        void onPreviewStart(@NonNull List<Size> sizes, @NonNull Size previewSize);
    }

    public interface OnFrameAvailableListener {
        void onFrameAvailable(byte[] data, Camera camera);
    }

    public interface OnFrameAvailable2Listener {
        void onFrameAvailable(Image image);
    }

    public interface OnFrameReleasedListener {
        void onFrameReleased(byte[] data);
    }

    public interface OnRecordingStartListener {
        void onRecordingStart(float fps, Size detectorSize);
    }

    public interface OnRecordingStopListener {
        void onRecordingStop(File file, boolean isVideoConfirmed);
    }

    public interface NetworkRequestStartListener {
        void onNetworkRequestStart(NetworkRequest request);
    }

    public interface NetworkRequestDoneListener {
        void onNetworkRequestDone(NetworkRequest request, Object responce);
    }

    public interface NetworkRequestErrorListener {
        void onNetworkRequestError(NetworkRequest request, Exception e);
    }

    public interface OnDetectionStateCahngedListener {
        void onDetectionStateChanged(@Nullable DetectionState oldState, @NonNull DetectionState newState);
    }

    public interface OnSwypeCodeSetListener {
        void onSwypeCodeSet(String swypeCode);
    }

    public interface OnDetectorPauseChangedListener {
        void onDetectorPauseChanged(boolean isPaused);
    }

    private class NetworkDelegate implements NetworkRequest.NetworkRequestListener {
        @Override
        public void onNetworkRequestStart(NetworkRequest request) {
            onNetworkRequestStart.postNotifyEvent(request);
        }

        @Override
        public void onNetworkRequestDone(NetworkRequest request, Object responce) {
            onNetworkRequestDone.postNotifyEvent(request, responce);
        }

        @Override
        public void onNetworkRequestError(NetworkRequest request, Exception e) {
            onNetworkRequestError.postNotifyEvent(request, e);
        }
    }
}
