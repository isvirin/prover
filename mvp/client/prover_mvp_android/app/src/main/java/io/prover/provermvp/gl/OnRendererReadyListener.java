package io.prover.provermvp.gl;

/**
 * Interface for callbacks when render thread completes its setup
 */
public interface OnRendererReadyListener {

    void onRendererReady();

    void onRendererFinished();
}
