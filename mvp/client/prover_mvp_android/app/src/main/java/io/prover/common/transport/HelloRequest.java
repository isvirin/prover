package io.prover.common.transport;

import org.ethereum.crypto.ECKey;
import org.json.JSONException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;

import io.prover.common.transport.responce.HelloResponce;

/**
 * Created by babay on 14.11.2017.
 */

public class HelloRequest extends NetworkRequest<HelloResponce> {
    private static final String METHOD = "hello";
    private final ECKey key;

    public HelloRequest(ECKey key, NetworkRequestListener listener) {
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
    protected HelloResponce parse(String source) throws IOException, JSONException {
        return new HelloResponce(source);
    }
}
