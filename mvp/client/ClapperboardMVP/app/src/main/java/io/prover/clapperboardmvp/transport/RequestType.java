package io.prover.clapperboardmvp.transport;

/**
 * Created by babay on 03.04.2017.
 */
public enum RequestType {
    Get, Post, Put, Delete;

    public boolean isEnclosing() {
        switch (this) {
            case Get:
            case Delete:
                return false;
            case Post:
            case Put:
                return true;
        }
        return false;
    }

    public String requestTypeString() {
        return this.name().toUpperCase();
    }
}
