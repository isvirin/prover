package io.prover.clapperboardmvp.transport;

import android.util.Log;

import org.ethereum.crypto.ECKey;
import org.spongycastle.util.BigIntegers;

import java.math.BigInteger;

import io.prover.clapperboardmvp.transport.responce.HelloResponce;

import static io.prover.clapperboardmvp.transport.NetworkRequest.TAG;

/**
 * Created by babay on 15.11.2017.
 */

public class NetworkSession {
    public final ECKey key;
    private volatile BigInteger actualNonce;
    private volatile HelloResponce hello;

    public NetworkSession(HelloResponce hello, ECKey key) {
        this.hello = hello;
        this.key = key;
        actualNonce = hello.bigIntegerNonce();

        Log.d(TAG, String.format("new netSession; nonce: %s, this: %s", actualNonce.toString(), this));
    }

    public void onNewHelloResponce(HelloResponce hello) {
        this.hello = hello;
        BigInteger oldNonce = actualNonce;
        synchronized (this) {
            BigInteger newNonce = hello.bigIntegerNonce();
            if (newNonce.compareTo(actualNonce) > 0)
                actualNonce = newNonce;
        }
        Log.d(TAG, String.format("update session hello, oldNonce: %s, newNonce: %s, selected: %s, this: %s",
                oldNonce.toString(), hello.bigIntegerNonce().toString(), actualNonce.toString(), this));
    }

    public BigInteger getNonce() {
        return actualNonce;
    }

    public byte[] getContractAddress() {
        return hello.contractAddress;
    }

    public byte[] getGasPrice() {
        return hello.gasPrice;
    }

    public BigInteger getGasPriceBigInt() {
        return BigIntegers.fromUnsignedByteArray(hello.gasPrice);
    }

    public BigInteger getMaxGasLimit(BigInteger gasPrice) {
        return hello.ethBalance.divide(gasPrice);
    }

    public void increaseNonce() {
        synchronized (this) {
            actualNonce = actualNonce.add(BigInteger.ONE);
        }
        Log.d(TAG, String.format("new nonce: %s, this: %s", actualNonce.toString(), this));
    }
}
