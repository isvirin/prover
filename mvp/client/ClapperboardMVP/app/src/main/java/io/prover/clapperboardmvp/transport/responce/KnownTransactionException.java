package io.prover.clapperboardmvp.transport.responce;

/**
 * Created by babay on 15.11.2017.
 */

public class KnownTransactionException extends FixableEtheriumException {
    public KnownTransactionException(String message) {
        super(message);
    }

    public KnownTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
