package io.prover.provermvp.transport.responce;

import java.io.IOException;

/**
 * Created by babay on 15.11.2017.
 */

public class TemporaryDenyException extends IOException {

    public TemporaryDenyException(String message) {
        super(message);
    }
}
