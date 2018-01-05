package io.prover.provermvp.transport;

import android.util.Log;

import org.ethereum.core.Transaction;
import org.json.JSONException;
import org.spongycastle.util.encoders.DecoderException;

import java.io.IOException;
import java.math.BigInteger;

import io.prover.provermvp.transport.responce.FixableEtheriumExcetion;
import io.prover.provermvp.transport.responce.SwypeResponce1;

/**
 * Created by babay on 14.11.2017.
 */

public class RequestSwypeCode1 extends NetworkRequest<SwypeResponce1> {

    static final String METHOD = "request-swype-code";

    private static final int GAS_LIMIT = 1_000_000;
    private static final int GENERATE_SWYPECODE_DATA = 0x74305b38;
    private final NetworkSession session;


    public RequestSwypeCode1(NetworkSession session, NetworkRequestListener listener) {
        super(listener);
        this.session = session;
    }

    @Override
    public void run() {
        listener.onNetworkRequestStart(this);
        try {
            SwypeResponce1 responce = postTransaction(METHOD, "hex=0x", null);
            session.increaseNonce();
            listener.onNetworkRequestDone(this, responce);
        } catch (FixableEtheriumExcetion e) {
            if (debugData != null) {
                debugData.setException(e).log();
            }
            session.increaseNonce();
            execute();
        } catch (IOException | DecoderException | JSONException ex) {
            handleException(ex);
        }
    }

    @Override
    protected SwypeResponce1 parse(String source) throws IOException, JSONException {
        return new SwypeResponce1(source, session.getNonce());
    }

    @Override
    protected Transaction createTransaction() {
        Log.d(TAG, "RequestSwypeCode1 nonce: " + session.getNonce().toString());
        byte[] gasLimit = toUnsignedByteArray(BigInteger.valueOf(GAS_LIMIT));
        byte[] data = toUnsignedByteArray(BigInteger.valueOf(GENERATE_SWYPECODE_DATA));
        byte[] nonce = toUnsignedByteArray(session.getNonce());
        Transaction transaction = new Transaction(nonce, session.getGasPrice(), gasLimit, session.getContractAddress(), new byte[]{0}, data);
        transaction.sign(session.key);
        return transaction;
    }
}
