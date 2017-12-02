package io.prover.provermvp.transport;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.ethereum.crypto.ECKey;

import java.io.File;

import io.prover.provermvp.Const;
import io.prover.provermvp.camera.Size;
import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.transport.responce.HelloResponce;
import io.prover.provermvp.transport.responce.SwypeResponce1;
import io.prover.provermvp.transport.responce.SwypeResponce2;

import static io.prover.provermvp.Settings.FAKE_SWYPE_CODE;
import static io.prover.provermvp.Settings.REQUEST_SWYPE;
import static io.prover.provermvp.transport.NetworkRequest.TAG;

/**
 * Created by babay on 14.11.2017.
 */

public class NetworkHolder implements CameraController.OnRecordingStopListener,
        CameraController.NetworkRequestDoneListener,
        CameraController.NetworkRequestErrorListener, CameraController.OnRecordingStartListener {
    private static final long REPEAT_SWYPE_REQUEST_2_TIMEOUT = 5_000;
    public final ECKey key;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final CameraController cameraController;
    private NetworkSession networkSession;
    private SwypeResponce1 swypeRequestHash;
    private SwypeResponce2 swypeResponce2;

    public NetworkHolder(ECKey key, CameraController cameraController) {
        this.key = key;
        this.cameraController = cameraController;
        cameraController.onRecordingStop.add(this);
        cameraController.onRecordingStart.add(this);
        cameraController.onNetworkRequestDone.add(this);
        cameraController.onNetworkRequestError.add(this);
    }

    public void doHello() {
        if (key != null) {
            new HelloRequest(key, cameraController.networkDelegate).execute();
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
                    new RequestSwypeCode2(swypeRequestHash, cameraController.networkDelegate).execute();
                }
            }, 10000);
        } else if (request instanceof RequestSwypeCode2) {
            swypeResponce2 = (SwypeResponce2) responce;
            cameraController.setSwypeCode(swypeResponce2.swypeCode);
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
            new SubmitVideoHashRequest(networkSession, swypeRequestHash, file, cameraController.networkDelegate)
                    .execute();
        }
    }

    @Override
    public void onRecordingStart(float fps, Size detectorSize) {
        swypeResponce2 = null;
        swypeRequestHash = null;
        if (networkSession != null && REQUEST_SWYPE) {
            new RequestSwypeCode1(networkSession, cameraController.networkDelegate).execute();
        } else if (FAKE_SWYPE_CODE) {
            genFakeSwypeCode();
        }
    }

    private void genFakeSwypeCode() {
        String code = Const.FAKE_SWYPES[(int) (Math.random() * Const.FAKE_SWYPES.length)];
        handler.postDelayed(() -> cameraController.setSwypeCode(code), 2000);
    }
}
