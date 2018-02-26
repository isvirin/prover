package io.prover.common.transport;

import org.ethereum.crypto.ECKey;
import org.json.JSONException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;

import io.prover.common.transport.responce.FastSwypeCodeResponce;

/**
 * Created by babay on 26.02.2018.
 */

public class FastRequestSwypeCode extends NetworkRequest<FastSwypeCodeResponce> {
    private static final String METHOD = "fast-request-swype-code";
    private final ECKey key;

    public FastRequestSwypeCode(ECKey key, NetworkRequestListener listener) {
        super(listener);
        this.key = key;
    }

    @Override
    public void run() {
        listener.onNetworkRequestStart(this);
        String requestBody = "user=0x" + Hex.toHexString(key.getAddress());
        execSimpleRequest(METHOD, RequestType.Post, requestBody);
    }

    @Override
    protected FastSwypeCodeResponce parse(String source) throws IOException, JSONException {
        return new FastSwypeCodeResponce(source);
    }

}
