package io.prover.common.transport.responce;

import java.io.IOException;

/**
 * Created by babay on 16.11.2017.
 */

public class LowFundsException extends IOException {
    public LowFundsException(String message) {
        super(message);
    }
}
