package io.prover.provermvp.transport;

import org.ethereum.core.Transaction;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;

import io.prover.provermvp.transport.responce.FixableEtheriumException;
import io.prover.provermvp.transport.responce.KnownTransactionException;
import io.prover.provermvp.transport.responce.LowFundsException;
import io.prover.provermvp.transport.responce.NonceTooLowException;

/**
 * Created by babay on 22.12.2017.
 */

public abstract class TransactionNetworkRequest<T> extends NetworkRequest<T> {



    protected final NetworkSession session;
    private final String method;

    public TransactionNetworkRequest(NetworkSession session, String method, NetworkRequestListener listener) {
        super(listener);
        this.session = session;
        this.method = method;
    }

    @Override
    public void run() {
        listener.onNetworkRequestStart(this);
        try {
            T responce = postTransaction(method, "hex=0x", null);
            session.increaseNonce();
            if (!cancelled) {
                listener.onNetworkRequestDone(this, responce);
            }
        } catch (FixableEtheriumException e) {
            if (debugData != null) {
                debugData.setException(e).log();
            }
            session.increaseNonce();
            execute();
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    protected T postTransaction(String method, String prefix, Transaction transaction) throws IOException, JSONException {
        if (transaction == null)
            transaction = createTransaction();
        byte[] bytes = transaction.getEncoded();
        byte[] encodedBytes = Hex.encode(bytes);
        String requestBody = prefix + new String(encodedBytes);
        String responceStr = postEnclosingRequest(method, RequestType.Post, requestBody);

        try {
            T responce = parse(responceStr);
            return responce;
        } catch (Exception e) {
            throw tryParseResponseException(responceStr, e);
        }
    }

    protected IOException tryParseResponseException(String responce, Exception e) {
        String message;
        try {
            JSONObject jso = new JSONObject(responce);
            JSONObject error = jso.getJSONObject("error");
            message = error.getString("message");
        } catch (Exception e1) {
            message = responce;
        }
        if (responce.contains("known transaction:")) {
            return new KnownTransactionException(message);
        }
        if (responce.contains("nonce too low")) {
            return new NonceTooLowException(message);
        }
        if (responce.contains("insufficient funds")) {
            return new LowFundsException(message);
        }
        return new IOException(e);
    }

    protected abstract Transaction createTransaction();
}
