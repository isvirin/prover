package io.prover.provermvp.transport.responce;

import java.io.IOException;

/**
 * Created by babay on 15.11.2017.
 */

public class FixableEtheriumExcetion extends IOException {
    public FixableEtheriumExcetion(String message) {
        super(message);
    }

    public FixableEtheriumExcetion(String message, Throwable cause) {
        super(message, cause);
    }

}
