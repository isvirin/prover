package io.prover.provermvp.controller;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.List;

import io.prover.provermvp.camera.Size;
import io.prover.provermvp.detector.DetectionState;
import io.prover.provermvp.transport.NetworkRequest;

/**
 * Created by babay on 11.12.2017.
 */

public class CameraControllerBase {

    public final NetworkDelegate networkDelegate = new NetworkDelegate();

    public final Handler handler = new Handler(Looper.getMainLooper());

    public final ListenerList2<OnPreviewStartListener, List<Size>, Size> previewStart
            = new ListenerList2<>(handler, OnPreviewStartListener::onPreviewStart);

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

    public final ListenerList2<OnSwypeCodeSetListener, String, String> swypeCodeSet
            = new ListenerList2<>(handler, OnSwypeCodeSetListener::onSwypeCodeSet);

    public final ListenerList2<OnFpsUpdateListener, Float, Float> fpsUpdateListener
            = new ListenerList2<>(handler, OnFpsUpdateListener::OnFpsUpdate);

    public final ListenerList<SwypeCodeConfirmedListener> swypeCodeConfirmed
            = new ListenerList<>(handler, SwypeCodeConfirmedListener::onSwypeCodeConfirmed);

    public interface OnPreviewStartListener {
        void onPreviewStart(@NonNull List<Size> sizes, @NonNull Size previewSize);
    }

    public interface OnFpsUpdateListener {
        void OnFpsUpdate(float fps, float processorFps);
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
        void onSwypeCodeSet(String swypeCode, String actualSwypeCode);
    }

    public interface SwypeCodeConfirmedListener {
        void onSwypeCodeConfirmed();
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
