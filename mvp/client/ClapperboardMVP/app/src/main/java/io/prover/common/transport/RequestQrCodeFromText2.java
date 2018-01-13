package io.prover.common.transport;

import org.json.JSONException;

import java.io.IOException;

import io.prover.common.transport.responce.HashResponce;
import io.prover.common.transport.responce.HashResponce2;

/**
 * Created by babay on 15.11.2017.
 */

public class RequestQrCodeFromText2 extends NetworkRequest<HashResponce2> {

    private final HashResponce responce1;

    public RequestQrCodeFromText2(HashResponce responce1, NetworkRequestListener listener) {
        super(listener);
        this.responce1 = responce1;
    }

    @Override
    public void run() {
        listener.onNetworkRequestStart(this);
        String requestBody = "txhash=" + responce1.hashString;
        execSimpleRequest(RequestQrCodeFromText1.METHOD, RequestType.Post, requestBody);
    }

    @Override
    protected HashResponce2 parse(String source) throws IOException, JSONException {
        return new HashResponce2(source, responce1);
    }
}
