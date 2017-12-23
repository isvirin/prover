package io.prover.clapperboardmvp.transport.responce;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;

import io.prover.clapperboardmvp.transport.BadResponceException;

/**
 * Created by babay on 14.11.2017.
 */

public class HelloResponce {
    public static final double BALANCE_DIV = 1e18;
    public final byte[] nonce;
    public final byte[] contractAddress;
    public final byte[] gasPrice;
    public final BigInteger ethBalance;

    // {"nonce": "0x1", "contractAddress": "0x6cc610c52ea25b8fd3786a4777f37c43a7010eb4", "gasPrice": "0x3b9aca00", "ethBalance": "0x15a6aa544e6a000"}
    public HelloResponce(String source) throws JSONException, IOException {
        JSONObject json = new JSONObject(source);

        contractAddress = Hex.decode(json.getString("contractAddress").substring(2));
        if (contractAddress.length != 20) {
            throw new BadResponceException("contact address is not 20 digits: " + contractAddress.length);
        }

        nonce = parseByteArrayString(json.getString("nonce").substring(2));
        gasPrice = parseByteArrayString(json.getString("gasPrice").substring(2));
        ethBalance = new BigInteger(json.getString("ethBalance").substring(2), 16);
    }

    private byte[] parseByteArrayString(String src) {
        if (src.length() % 2 == 1)
            src = "0" + src;
        return Hex.decode(src);
    }

    public double getDoubleBalance() {
        return ethBalance.doubleValue() / BALANCE_DIV;
    }

    public BigInteger bigIntegerNonce() {
        return new BigInteger(1, nonce);
    }
}
