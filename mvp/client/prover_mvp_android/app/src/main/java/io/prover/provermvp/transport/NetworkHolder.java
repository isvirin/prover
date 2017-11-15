package io.prover.provermvp.transport;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.ethereum.crypto.ECKey;

import java.io.File;

import io.prover.provermvp.transport.responce.HelloResponce;
import io.prover.provermvp.transport.responce.SwypeResponce1;
import io.prover.provermvp.transport.responce.SwypeResponce2;

import static io.prover.provermvp.Settings.REQUEST_SWYPE;
import static io.prover.provermvp.transport.NetworkRequest.TAG;

/**
 * Created by babay on 14.11.2017.
 */

public class NetworkHolder implements NetworkRequest.NetworkRequestListener {
    private static final long REPEAT_SWYPE_REQUEST_2_TIMEOUT = 5_000;
    public final ECKey key;
    public final NetworkRequest.NetworkRequestListener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private NetworkSession networkSession;
    private SwypeResponce1 swypeRequestHash;
    private SwypeResponce2 swypeResponce2;

    public NetworkHolder(ECKey key, NetworkRequest.NetworkRequestListener listener) {
        this.key = key;
        this.listener = listener;
    }

    public void doHello() {
        if (key != null) {
            new HelloRequest(key, this).execute();
        }
    }

    public boolean requestSwypeCode() {
        swypeResponce2 = null;
        swypeRequestHash = null;
        if (networkSession != null && REQUEST_SWYPE) {
            new RequestSwypeCode1(networkSession, this).execute();
            return true;
        }
        return false;
    }

    /**
     * if file == null then recording was aborted
     *
     * @param file
     */
    public void onStopRecording(File file) {
        if (swypeResponce2 == null) {
            swypeRequestHash = null;
        } else if (file != null) {
            new SubmitVideoHashRequest(networkSession, swypeRequestHash, file, this)
                    .execute();
        }
    }

    @Override
    public void onNetworkRequestDone(NetworkRequest request, Object responce) {
        if (request instanceof HelloRequest) {
            networkSession = new NetworkSession((HelloResponce) responce, key, networkSession);
        } else if (request instanceof RequestSwypeCode1) {
            swypeRequestHash = (SwypeResponce1) responce;
            handler.postDelayed(() -> {
                if (swypeRequestHash != null) {
                    new RequestSwypeCode2(swypeRequestHash, this).execute();
                }
            }, 10000);
        } else if (request instanceof RequestSwypeCode2) {
            swypeResponce2 = (SwypeResponce2) responce;
        }
        listener.onNetworkRequestDone(request, responce);
    }

    @Override
    public void onNetworkRequestError(NetworkRequest request, Exception e) {
        listener.onNetworkRequestError(request, e);
        if (request instanceof RequestSwypeCode2 && swypeRequestHash != null) {
            handler.postDelayed(() -> {
                if (swypeRequestHash != null)
                    request.execute();
            }, REPEAT_SWYPE_REQUEST_2_TIMEOUT);
            Log.d(TAG, "postponed RequestSwypeCode2");
        }
    }
}
