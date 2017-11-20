package io.prover.provermvp.controller;

import android.hardware.Camera;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.List;

import io.prover.provermvp.camera.Size;
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

    public final ListenerList1<OnRecordingStartListener, Float> onRecordingStart
            = new ListenerList1<>(handler, OnRecordingStartListener::onRecordingStart);

    public final ListenerList2<OnRecordingStopListener, File, Boolean> onRecordingStop
            = new ListenerList2<>(handler, OnRecordingStopListener::onRecordingStop);

    public final ListenerList1<NetworkRequestStartListener, NetworkRequest> onNetworkRequestStart
            = new ListenerList1<>(handler, NetworkRequestStartListener::onNetworkRequestStart);

    public final ListenerList2<NetworkRequestDoneListener, NetworkRequest, Object> networkRequestDone
            = new ListenerList2<>(handler, NetworkRequestDoneListener::onNetworkRequestDone);

    public final ListenerList2<NetworkRequestErrorListener, NetworkRequest, Exception> networkRequestError
            = new ListenerList2<>(handler, NetworkRequestErrorListener::onNetworkRequestError);
    public final NetworkDelegate networkDelegate = new NetworkDelegate();
    private boolean recording;
    private SwypeStateHelperHolder swypeStateHelperHolder;


    public CameraController() {
    }

    public boolean isRecording() {
        return recording;
    }

    public void onRecordingStart(float averageFps) {
        recording = true;
        onRecordingStart.postNotifyEvent(averageFps);
    }

    public void onRecordingStop(File file) {
        recording = false;
        boolean isVideoConfirmed = swypeStateHelperHolder.isVideoConfirmed();
        onRecordingStop.notifyEvent(file, isVideoConfirmed);
    }

    public void setSwypeStateHelperHolder(SwypeStateHelperHolder swypeStateHelperHolder) {
        this.swypeStateHelperHolder = swypeStateHelperHolder;
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
        void onRecordingStart(float fps);
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

    private class NetworkDelegate implements NetworkRequest.NetworkRequestListener {
        @Override
        public void onNetworkRequestStart(NetworkRequest request) {
            onNetworkRequestStart.postNotifyEvent(request);
        }

        @Override
        public void onNetworkRequestDone(NetworkRequest request, Object responce) {
            networkRequestDone.postNotifyEvent(request, responce);
        }

        @Override
        public void onNetworkRequestError(NetworkRequest request, Exception e) {
            networkRequestError.postNotifyEvent(request, e);
        }
    }
}
