package io.prover.provermvp.transport.responce;

/**
 * Created by babay on 15.11.2017.
 */

public class NonceTooLowException extends FixableEtheriumExcetion {
    public NonceTooLowException(String message) {
        super(message);
    }

    public NonceTooLowException(String message, Throwable cause) {
        super(message, cause);
    }
}
