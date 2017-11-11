package io.prover.provermvp.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import io.prover.provermvp.R;
import io.prover.provermvp.detector.DetectionState;
import io.prover.provermvp.detector.ProverDetector;

/**
 * Created by babay on 11.11.2017.
 */

public class SwypeStateHelperHolder implements ProverDetector.DetectionListener {
    private final ViewGroup root;
    private final TextView statsText;

    public SwypeStateHelperHolder(ViewGroup root) {
        this.root = root;
        statsText = root.findViewById(R.id.statsView);
        statsText.bringToFront();
    }

    @Override
    public void onDetectionStateChanged(@Nullable DetectionState oldState, @NonNull DetectionState state) {
        statsText.setText(String.format(Locale.getDefault(), "%d, %d, %d, %d", state.state, state.index, state.x, state.y));
    }
}
