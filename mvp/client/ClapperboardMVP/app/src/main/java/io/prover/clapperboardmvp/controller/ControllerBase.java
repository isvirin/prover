package io.prover.clapperboardmvp.controller;

import android.os.Handler;
import android.os.Looper;

import io.prover.clapperboardmvp.viewholder.ScreenLogger;
import io.prover.common.transport.NetworkRequest;

/**
 * Created by babay on 11.12.2017.
 */

public class ControllerBase {

    public final NetworkDelegate networkDelegate = new NetworkDelegate();

    public final Handler handler = new Handler(Looper.getMainLooper());

    public final ListenerList1<NetworkRequestStartListener, NetworkRequest> onNetworkRequestStart
            = new ListenerList1<>(handler, NetworkRequestStartListener::onNetworkRequestStart);

    public final ListenerList2<NetworkRequestDoneListener, NetworkRequest, Object> onNetworkRequestDone
            = new ListenerList2<>(handler, NetworkRequestDoneListener::onNetworkRequestDone);

    public final ListenerList2<NetworkRequestErrorListener, NetworkRequest, Exception> onNetworkRequestError
            = new ListenerList2<>(handler, NetworkRequestErrorListener::onNetworkRequestError);

    public volatile boolean enableScreenLog;
    private ScreenLogger screenLogger;

    public void setScreenLogger(ScreenLogger screenLogger) {
        if (this.screenLogger != null) {
            this.screenLogger.removeFromParent();
        }
        this.screenLogger = screenLogger;
        enableScreenLog = screenLogger != null;
    }

    public void addToScreenLog(CharSequence text) {
        if (screenLogger != null) {
            if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
                screenLogger.addText(text);
            } else {
                handler.post(() -> {
                    if (screenLogger != null) screenLogger.addText(text);
                });
            }
        }
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
            onNetworkRequestDone.postNotifyEvent(request, responce);
        }

        @Override
        public void onNetworkRequestError(NetworkRequest request, Exception e) {
            onNetworkRequestError.postNotifyEvent(request, e);
        }
    }
}
