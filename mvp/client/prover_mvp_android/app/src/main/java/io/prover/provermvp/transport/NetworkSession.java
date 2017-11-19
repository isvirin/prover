package io.prover.provermvp.transport;

import android.util.Log;

import org.ethereum.crypto.ECKey;

import java.math.BigInteger;

import io.prover.provermvp.transport.responce.HelloResponce;

import static io.prover.provermvp.transport.NetworkRequest.TAG;

/**
 * Created by babay on 15.11.2017.
 */

public class NetworkSession {
    public final byte[] contractAddress;
    public final byte[] gasPrice;
    public final ECKey key;
    public volatile BigInteger ethBalance;
    private volatile BigInteger acualNonce;

    public NetworkSession(HelloResponce responce, ECKey key, NetworkSession oldNetworkSession) {
        contractAddress = responce.contractAddress;
        gasPrice = responce.gasPrice;
        ethBalance = responce.ethBalance;
        this.key = key;
        acualNonce = responce.bigIntegerNonce();
        if (oldNetworkSession != null && oldNetworkSession.acualNonce.compareTo(acualNonce) > 0) {
            acualNonce = oldNetworkSession.acualNonce;
        }

        Log.d(TAG, String.format("new netSession; old nonce: %s, new nonce: %s, selectedNonce : %s",
                oldNetworkSession == null ? null : oldNetworkSession.acualNonce.toString(),
                acualNonce.toString(), responce.bigIntegerNonce().toString()));
    }

    public BigInteger getNonce() {
        return acualNonce;
    }

    public void increaseNonce() {
        acualNonce = acualNonce.add(BigInteger.ONE);
    }
}
