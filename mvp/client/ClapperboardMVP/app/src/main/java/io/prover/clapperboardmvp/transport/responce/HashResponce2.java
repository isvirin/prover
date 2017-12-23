package io.prover.clapperboardmvp.transport.responce;

import org.json.JSONException;

/**
 * Created by babay on 22.12.2017.
 */

public class HashResponce2 extends HashResponce {
    public final HashResponce hashResponce1;

    public HashResponce2(String source, HashResponce hashResponce1) throws JSONException, TemporaryDenyException {
        super(source);
        this.hashResponce1 = hashResponce1;
    }

    public HashResponce2(byte[] source, HashResponce hashResponce1) {
        super(source);
        this.hashResponce1 = hashResponce1;
    }

    public static HashResponce2 random() {
        byte[] hash1 = new byte[32];
        byte[] hash2 = new byte[32];
        for (int i = 0; i < hash1.length; i++) {
            hash1[i] = (byte) (Math.random() * 256);
            hash2[i] = (byte) (Math.random() * 256);
        }
        return new HashResponce2(hash2, new HashResponce(hash1));
    }
}
