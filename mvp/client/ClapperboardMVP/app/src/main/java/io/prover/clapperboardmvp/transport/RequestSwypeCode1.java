package io.prover.clapperboardmvp.transport;

import android.util.Log;

import org.ethereum.core.Transaction;
import org.json.JSONException;
import org.spongycastle.util.BigIntegers;

import java.io.IOException;
import java.math.BigInteger;

import io.prover.clapperboardmvp.transport.responce.SwypeResponce1;

/**
 * Created by babay on 14.11.2017.
 */

public class RequestSwypeCode1 extends TransactionNetworkRequest<SwypeResponce1> {

    protected static final int GAS_LIMIT = 60_000;
    static final String METHOD = "request-swype-code";
    private static final int GENERATE_SWYPECODE_DATA = 0x74305b38;


    public RequestSwypeCode1(NetworkSession session, NetworkRequestListener listener) {
        super(session, METHOD, listener);
    }

    @Override
    protected SwypeResponce1 parse(String source) throws IOException, JSONException {
        return new SwypeResponce1(source);
    }

    @Override
    protected Transaction createTransaction() {
        Log.d(TAG, "RequestSwypeCode1 nonce: " + session.getNonce().toString());
        byte[] gasLimit = BigIntegers.asUnsignedByteArray(BigInteger.valueOf(GAS_LIMIT));
        byte[] data = BigIntegers.asUnsignedByteArray(BigInteger.valueOf(GENERATE_SWYPECODE_DATA));
        byte[] nonce = BigIntegers.asUnsignedByteArray(session.getNonce());

        BigInteger price = session.getGasPriceBigInt().multiply(BigInteger.valueOf(3)).divide(BigInteger.valueOf(2));
        byte[] gasPrice = BigIntegers.asUnsignedByteArray(price);

        Transaction transaction = new Transaction(nonce, gasPrice, gasLimit, session.getContractAddress(), new byte[]{0}, data);
        transaction.sign(session.key);
        return transaction;
    }
}
