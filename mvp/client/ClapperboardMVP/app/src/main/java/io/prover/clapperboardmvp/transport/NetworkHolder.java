package io.prover.clapperboardmvp.transport;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.ethereum.crypto.ECKey;

import java.util.ArrayList;

import io.prover.clapperboardmvp.controller.Controller;
import io.prover.common.transport.HelloRequest;
import io.prover.common.transport.NetworkRequest;
import io.prover.common.transport.NetworkSession;
import io.prover.common.transport.RequestQrCodeFromText1;
import io.prover.common.transport.RequestQrCodeFromText2;
import io.prover.common.transport.RequestSwypeCode1;
import io.prover.common.transport.RequestSwypeCode2;
import io.prover.common.transport.responce.HashResponce;
import io.prover.common.transport.responce.HelloResponce;
import io.prover.common.transport.responce.SwypeResponce1;
import io.prover.common.transport.responce.TemporaryDenyException;
import io.prover.common.util.Etherium;

import static io.prover.common.transport.NetworkRequest.TAG;

/**
 * Created by babay on 14.11.2017.
 */

public class NetworkHolder implements Controller.NetworkRequestDoneListener,
        Controller.NetworkRequestErrorListener {
    private static final long REPEAT_CHECK_TRANSACTION_TIMEOUT = 5_000;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Controller controller;
    private final ArrayList<NetworkRequest> requests = new ArrayList<>();
    private final Etherium etherium;
    private NetworkSession networkSession;
    private SwypeResponce1 swypeRequestHash;

    public NetworkHolder(Context context, Controller controller) {
        etherium = Etherium.getInstance(context);
        this.controller = controller;
        controller.onNetworkRequestDone.add(this);
        controller.onNetworkRequestError.add(this);
    }

    public void doHello() {
        ECKey key = etherium.getKey();
        if (key != null) {
            execNetworkRequest(new HelloRequest(key, controller.networkDelegate));
        }
    }

    public void submitMessageForQrCode(String message) {

        if (networkSession == null) {
            doHello();
            controller.handler.postDelayed(() -> {
                if (networkSession != null)
                    execNetworkRequest(new RequestQrCodeFromText1(networkSession, message, controller.networkDelegate));
            }, 1000);
        } else {
            execNetworkRequest(new RequestQrCodeFromText1(networkSession, message, controller.networkDelegate));
        }
    }

    @Override
    public void onNetworkRequestDone(NetworkRequest request, Object responce) {
        if (request.isCancelled())
            return;
        onNetworkRequestFinished(request);
        if (request instanceof HelloRequest) {
            ECKey key = etherium.getKey();
            if (networkSession != null && networkSession.key.equals(key)) {
                networkSession.onNewHelloResponce((HelloResponce) responce);
            } else {
                networkSession = new NetworkSession((HelloResponce) responce, key);
            }
        } else if (request instanceof RequestSwypeCode1) {
            swypeRequestHash = (SwypeResponce1) responce;
            handler.postDelayed(() -> {
                if (swypeRequestHash != null) {
                    execNetworkRequest(new RequestSwypeCode2(swypeRequestHash, controller.networkDelegate));
                }
            }, 10_000);
        } else if (request instanceof RequestQrCodeFromText1) {
            NetworkRequest newRequest = new RequestQrCodeFromText2((HashResponce) responce, controller.networkDelegate);
            postExecNetworkRequest(newRequest, 10_000);
        }
    }

    @Override
    public void onNetworkRequestError(NetworkRequest request, Exception e) {
        if (request.isCancelled())
            return;
        if (e instanceof TemporaryDenyException) {
            handler.postDelayed(request::execute, REPEAT_CHECK_TRANSACTION_TIMEOUT);
            Log.d(TAG, "postponed wait for transaction result");
        } else {
            onNetworkRequestFinished(request);
        }
    }

    private void execNetworkRequest(NetworkRequest request) {
        synchronized (requests) {
            requests.add(request);
        }
        request.execute();
    }

    private void postExecNetworkRequest(NetworkRequest request, long delay) {
        synchronized (requests) {
            requests.add(request);
        }
        handler.postDelayed(request::execute, delay);
    }

    private void onNetworkRequestFinished(NetworkRequest request) {
        synchronized (requests) {
            requests.remove(request);
        }
    }

    public int getTotalRequestsCounter() {
        synchronized (requests) {
            return requests.size();
        }
    }

    public void cancelAllRequests() {
        for (NetworkRequest request : requests) {
            request.cancel();
        }
        requests.clear();
    }
}
