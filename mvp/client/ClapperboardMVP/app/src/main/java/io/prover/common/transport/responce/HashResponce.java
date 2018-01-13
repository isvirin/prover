package io.prover.common.transport.responce;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * Created by babay on 22.12.2017.
 */

public class HashResponce {
    public final String hashString;
    public final byte[] hashBytes;

    public HashResponce(String source) throws JSONException, TemporaryDenyException {
        //{"result": "0x717e92c8c501ca13e5ad7d9e240457d3e5208ff90da82ea126c9ed35881f6eda"}

        JSONObject obj = new JSONObject(source);
        if (obj.isNull("result")) {
            throw new TemporaryDenyException("Responce in not ready yet");
        }

        hashString = obj.getString("result");
        BigInteger resultBigInt = new BigInteger(hashString.substring(2), 16);
        hashBytes = BigIntegers.asUnsignedByteArray(32, resultBigInt);
    }

    protected HashResponce(byte[] source) {
        hashBytes = source;
        hashString = "0x" + Hex.toHexString(source);
    }
}
