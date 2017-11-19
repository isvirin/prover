package io.prover.provermvp.transport.responce;

/**
 * Created by babay on 15.11.2017.
 */

public class KnownTransactionException extends FixableEtheriumExcetion {
    public KnownTransactionException(String message) {
        super(message);
    }

    public KnownTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
