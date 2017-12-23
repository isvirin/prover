package io.prover.clapperboardmvp.transport;

import java.io.IOException;

/**
 * Created by babay on 14.11.2017.
 */

public class BadResponceException extends IOException {
    public BadResponceException(String message) {
        super(message);
    }
}
