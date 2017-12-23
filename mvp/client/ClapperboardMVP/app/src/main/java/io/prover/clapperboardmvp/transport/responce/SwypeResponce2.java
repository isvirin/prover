package io.prover.clapperboardmvp.transport.responce;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by babay on 15.11.2017.
 */

public class SwypeResponce2 {
    public final String swypeCode;
    public final int swypeId;

    //{"result": {"swype-id": 16164, "swype-sequence": [5, 6, 8, 9, 8, 5, 1, 2]}}

    public SwypeResponce2(String source) throws JSONException, TemporaryDenyException {
        JSONObject obj = new JSONObject(source);
        if (obj.isNull("result")) {
            throw new TemporaryDenyException("Responce in not ready yet");
        }
        JSONObject resultJso = obj.getJSONObject("result");
        swypeId = resultJso.getInt("swype-id");
        JSONArray swipeSequence = resultJso.getJSONArray("swype-sequence");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < swipeSequence.length(); i++) {
            builder.append(swipeSequence.getInt(i));
        }
        swypeCode = builder.toString();
    }
}
