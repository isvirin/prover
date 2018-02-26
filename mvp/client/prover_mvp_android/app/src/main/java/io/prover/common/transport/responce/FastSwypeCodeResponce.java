package io.prover.common.transport.responce;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;


/**
 * Created by babay on 27.02.2018.
 */

public class FastSwypeCodeResponce {
    public final String referenceBlockString;
    public final byte[] referenceBlockBytes;
    public final String swypeCode;
    public final int swypeId;

    public FastSwypeCodeResponce(String responce) throws JSONException {
        JSONObject source = new JSONObject(responce);
        JSONObject resJson = source.getJSONObject("result");
        swypeId = resJson.getInt("swype-id");
        JSONArray swypeSequence = resJson.getJSONArray("swype-sequence");

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < swypeSequence.length(); i++) {
            builder.append(swypeSequence.getInt(i));
        }
        swypeCode = builder.toString();

        referenceBlockString = resJson.getString("reference-block");
        byte[] hashBytes = Hex.decode(referenceBlockString.substring(2));
        if (hashBytes.length == 32)
            this.referenceBlockBytes = hashBytes;
        else {
            this.referenceBlockBytes = new byte[32];
            System.arraycopy(hashBytes, 0, referenceBlockBytes, 32 - hashBytes.length, hashBytes.length);
        }
    }
}
