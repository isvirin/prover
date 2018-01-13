package io.prover.provermvp.transport;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.ethereum.crypto.ECKey;

import java.io.File;
import java.util.ArrayList;

import io.prover.common.transport.HelloRequest;
import io.prover.common.transport.NetworkRequest;
import io.prover.common.transport.NetworkSession;
import io.prover.common.transport.RequestSwypeCode1;
import io.prover.common.transport.RequestSwypeCode2;
import io.prover.common.transport.SubmitVideoHashRequest;
import io.prover.common.transport.responce.HelloResponce;
import io.prover.common.transport.responce.SwypeResponce1;
import io.prover.common.transport.responce.SwypeResponce2;
import io.prover.common.util.Etherium;
import io.prover.provermvp.Const;
import io.prover.provermvp.controller.CameraController;

import static io.prover.common.transport.NetworkRequest.TAG;
import static io.prover.provermvp.Settings.FAKE_SWYPE_CODE;
import static io.prover.provermvp.Settings.REQUEST_SWYPE;

/**
 * Created by babay on 14.11.2017.
 */

public class NetworkHolder implements CameraController.OnRecordingStopListener,
        CameraController.NetworkRequestDoneListener,
        CameraController.NetworkRequestErrorListener, CameraController.OnRecordingStartListener {
    private static final long REPEAT_SWYPE_REQUEST_2_TIMEOUT = 5_000;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final CameraController cameraController;
    private final ArrayList<NetworkRequest> requests = new ArrayList<>();
    private final Etherium etherium;
    private NetworkSession networkSession;
    private SwypeResponce1 swypeRequestHash;
    private SwypeResponce2 swypeResponce2;

    public NetworkHolder(Context context, CameraController cameraController) {
        etherium = Etherium.getInstance(context);
        this.cameraController = cameraController;
        cameraController.onRecordingStop.add(this);
        cameraController.onRecordingStart.add(this);
        cameraController.onNetworkRequestDone.add(this);
        cameraController.onNetworkRequestError.add(this);
    }

    public void doHello() {
        ECKey key = etherium.getKey();
        if (key != null) {
            execNetworkRequest(new HelloRequest(key, cameraController.networkDelegate));
        }
    }

    @Override
    public void onNetworkRequestDone(NetworkRequest request, Object responce) {
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
                    execNetworkRequest(new RequestSwypeCode2(swypeRequestHash, cameraController.networkDelegate));
                }
            }, 10000);
        } else if (request instanceof RequestSwypeCode2) {
            swypeResponce2 = (SwypeResponce2) responce;
            cameraController.setSwypeCode(swypeResponce2.swypeCode);
            handler.postDelayed(this::doHello, 30_000);
        } else if (request instanceof SubmitVideoHashRequest) {
            handler.postDelayed(this::doHello, 30_000);
        }
    }

    @Override
    public void onNetworkRequestError(NetworkRequest request, Exception e) {
        if (request instanceof RequestSwypeCode2 && swypeRequestHash != null) {
            handler.postDelayed(() -> {
                if (swypeRequestHash != null)
                    request.execute();
            }, REPEAT_SWYPE_REQUEST_2_TIMEOUT);
            Log.d(TAG, "postponed RequestSwypeCode2");
        } else {
            onNetworkRequestFinished(request);
        }
        if (request instanceof RequestSwypeCode1 && FAKE_SWYPE_CODE) {
            genFakeSwypeCode();
        }
    }

    @Override
    public void onRecordingStop(File file, boolean isVideoConfirmed) {
        if (swypeResponce2 == null) {
            swypeRequestHash = null;
        } else if (file != null && isVideoConfirmed) {
            execNetworkRequest(new SubmitVideoHashRequest(networkSession, swypeRequestHash, file, cameraController.networkDelegate));
        }
    }

    @Override
    public void onRecordingStart() {
        swypeResponce2 = null;
        swypeRequestHash = null;
        if (networkSession != null && REQUEST_SWYPE) {
            execNetworkRequest(new RequestSwypeCode1(networkSession, cameraController.networkDelegate));
        } else if (FAKE_SWYPE_CODE) {
            genFakeSwypeCode();
        }
    }

    private void genFakeSwypeCode() {
        String code = Const.FAKE_SWYPES[(int) (Math.random() * Const.FAKE_SWYPES.length)];
        handler.postDelayed(() -> cameraController.setSwypeCode(code), 2000);
    }

    private void execNetworkRequest(NetworkRequest request) {
        synchronized (requests) {
            requests.add(request);
        }
        request.execute();
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
}
