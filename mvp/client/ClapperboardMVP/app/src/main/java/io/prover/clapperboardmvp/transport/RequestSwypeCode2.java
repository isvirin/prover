package io.prover.clapperboardmvp.transport;

import org.json.JSONException;

import java.io.IOException;

import io.prover.clapperboardmvp.transport.responce.SwypeResponce1;
import io.prover.clapperboardmvp.transport.responce.SwypeResponce2;

/**
 * Created by babay on 15.11.2017.
 */

public class RequestSwypeCode2 extends NetworkRequest<SwypeResponce2> {

    private final SwypeResponce1 responce1;

    public RequestSwypeCode2(SwypeResponce1 responce1, NetworkRequestListener listener) {
        super(listener);
        this.responce1 = responce1;
    }

    @Override
    public void run() {
        listener.onNetworkRequestStart(this);
        String requestBody = "txhash=" + responce1.hashString;
        execSimpleRequest(RequestSwypeCode1.METHOD, RequestType.Post, requestBody);
    }

    @Override
    protected SwypeResponce2 parse(String source) throws IOException, JSONException {
        return new SwypeResponce2(source);
    }
}
