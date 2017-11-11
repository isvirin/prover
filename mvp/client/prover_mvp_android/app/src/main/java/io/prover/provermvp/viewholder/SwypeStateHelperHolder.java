package io.prover.provermvp.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import io.prover.provermvp.detector.DetectionState;
import io.prover.provermvp.detector.ProverDetector;

/**
 * Created by babay on 11.11.2017.
 */

public class SwypeStateHelperHolder implements ProverDetector.DetectionListener {
    private final ViewGroup root;

    public SwypeStateHelperHolder(ViewGroup root) {
        this.root = root;
    }

    @Override
    public void onDetectionStateChanged(@Nullable DetectionState oldState, @NonNull DetectionState newState) {

    }
}
