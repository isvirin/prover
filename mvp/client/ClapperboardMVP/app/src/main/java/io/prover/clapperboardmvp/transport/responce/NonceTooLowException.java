package io.prover.clapperboardmvp.transport.responce;

/**
 * Created by babay on 15.11.2017.
 */

public class NonceTooLowException extends FixableEtheriumException {
    public NonceTooLowException(String message) {
        super(message);
    }

    public NonceTooLowException(String message, Throwable cause) {
        super(message, cause);
    }
}
