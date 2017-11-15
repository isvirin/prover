package io.prover.provermvp.transport.responce;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by babay on 15.11.2017.
 */

public class SwypeResponce2 {
    public final String swypeCode;

    public SwypeResponce2(String source) throws JSONException, TemporaryDenyException {
        JSONObject obj = new JSONObject(source);
        if (obj.isNull("result")) {
            throw new TemporaryDenyException("Responce in not ready yet");
        }
        int swypeCode = obj.getInt("result");
        this.swypeCode = Integer.toString(swypeCode);
    }
}
