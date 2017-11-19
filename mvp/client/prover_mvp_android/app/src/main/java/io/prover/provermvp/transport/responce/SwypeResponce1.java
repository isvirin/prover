package io.prover.provermvp.transport.responce;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * Created by babay on 15.11.2017.
 */

public class SwypeResponce1 {
    public final String hashString;
    public final byte[] hashBytes;
    public final BigInteger requestNonce;

    public SwypeResponce1(String responce, BigInteger requestNonce) throws DecoderException, JSONException {
        JSONObject responceJson = new JSONObject(responce);
        this.hashString = responceJson.getString("result");
        hashBytes = Hex.decode(hashString.substring(2));
        this.requestNonce = requestNonce;
    }
}
