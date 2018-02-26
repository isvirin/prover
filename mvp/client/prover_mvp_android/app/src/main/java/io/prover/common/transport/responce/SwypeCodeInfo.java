package io.prover.common.transport.responce;

/**
 * Created by babay on 27.02.2018.
 */

public class SwypeCodeInfo {
    public final String swypeCode;
    public final int swypeId;
    public final byte[] hashBytes;

    public SwypeCodeInfo(SwypeResponce1 responce1, SwypeResponce2 responce2) {
        hashBytes = responce1.hashBytes;
        swypeCode = responce2.swypeCode;
        swypeId = responce2.swypeId;
    }

    public SwypeCodeInfo(FastSwypeCodeResponce responce) {
        swypeId = responce.swypeId;
        swypeCode = responce.swypeCode;
        hashBytes = responce.referenceBlockBytes;
    }
}
