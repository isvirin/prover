package io.prover.provermvp.transport.responce;

import java.io.IOException;

/**
 * Created by babay on 15.11.2017.
 */

public class FixableEtheriumException extends IOException {
    public FixableEtheriumException(String message) {
        super(message);
    }

    public FixableEtheriumException(String message, Throwable cause) {
        super(message, cause);
    }

}
