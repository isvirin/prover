package io.prover.common.transport.responce;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by babay on 15.11.2017.
 */

public class SwypeResponce1 {
    public final String hashString;
    public final byte[] hashBytes;

    public SwypeResponce1(String responce) throws DecoderException, JSONException {
        JSONObject responceJson = new JSONObject(responce);
        this.hashString = responceJson.getString("result");
        byte[] hashBytes = Hex.decode(hashString.substring(2));
        if (hashBytes.length == 32)
            this.hashBytes = hashBytes;
        else {
            this.hashBytes = new byte[32];
            System.arraycopy(hashBytes, 0, this.hashBytes, 32 - hashBytes.length, hashBytes.length);
        }
    }
}
