package io.prover.provermvp.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.view.View;
import android.widget.ImageView;

import io.prover.provermvp.R;
import io.prover.provermvp.camera.Size;
import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.detector.DetectionState;

/**
 * Created by babay on 22.11.2017.
 */

public class SwypeViewHolder implements CameraController.OnDetectionStateCahngedListener,
        CameraController.OnSwypeCodeSetListener, CameraController.OnRecordingStartListener {
    private final ConstraintLayout root;
    private final CameraController cameraController;
    private final ImageView[] swypePoints = new ImageView[9];
    private final ImageView currentPoint;
    float xMult, yMult;
    private String swype;
    private ImageView[] swypeSequence;
    private int[] sequenceIndices;
    private Size detectorSize;
    private int detectProgressPos;

    private VectorDrawableCompat pathPointDrawable;
    private VectorDrawableCompat empptyPointDrawable;
    private VectorDrawableCompat visitedPointDrawable;


    public SwypeViewHolder(ConstraintLayout root, CameraController cameraController) {
        this.root = root;
        this.cameraController = cameraController;

        swypePoints[0] = root.findViewById(R.id.swypePoint1);
        swypePoints[1] = root.findViewById(R.id.swypePoint2);
        swypePoints[2] = root.findViewById(R.id.swypePoint3);
        swypePoints[3] = root.findViewById(R.id.swypePoint4);
        swypePoints[4] = root.findViewById(R.id.swypePoint5);
        swypePoints[5] = root.findViewById(R.id.swypePoint6);
        swypePoints[6] = root.findViewById(R.id.swypePoint7);
        swypePoints[7] = root.findViewById(R.id.swypePoint8);
        swypePoints[8] = root.findViewById(R.id.swypePoint9);

        currentPoint = root.findViewById(R.id.swypeCurrentPosition);

        cameraController.detectionState.add(this);
        cameraController.swypeCodeSet.add(this);
        cameraController.onRecordingStart.add(this);
        root.setVisibility(View.GONE);
    }

    @Override
    public void onDetectionStateChanged(@Nullable DetectionState oldState, @NonNull DetectionState newState) {
        boolean visible = root.getVisibility() == View.VISIBLE;
        boolean shouldBeVisible = newState.state == 2 || newState.index != 0;
        if (visible != shouldBeVisible) {
            root.setVisibility(shouldBeVisible ? View.VISIBLE : View.GONE);
            if (shouldBeVisible) {
                resetDetectionPosition();
            }
        }

        if (!shouldBeVisible || swypeSequence == null || pathPointDrawable == null)
            return;

        if (newState.x != 0 || newState.y != 0 || newState.index != 0) {
            currentPoint.setTranslationX(newState.x * xMult);
            currentPoint.setTranslationY(newState.y * yMult);
        }
        if (detectProgressPos < newState.index && newState.index < swypeSequence.length) {
            if (detectProgressPos >= 0) {
                swypeSequence[detectProgressPos].setImageDrawable(visitedPointDrawable);
            }
            swypeSequence[newState.index].setImageDrawable(visitedPointDrawable);
            detectProgressPos = newState.index;
        }
    }

    @Override
    public void onSwypeCodeSet(String swypeCode) {
        this.swype = swype;

        loadDrawables();
        swypeSequence = new ImageView[swypeCode.length()];
        sequenceIndices = new int[swypeCode.length()];
        char[] charArray = swypeCode.toCharArray();

        for (ImageView swypePoint : swypePoints) {
            swypePoint.setImageDrawable(empptyPointDrawable);
        }
        for (int i = 0; i < charArray.length; i++) {
            char ch = charArray[i];
            int pos = ch - '1';
            if (pos >= 0 && pos < 9) {
                swypeSequence[i] = swypePoints[pos];
                swypeSequence[i].setImageDrawable(pathPointDrawable);
                sequenceIndices[i] = pos;
            }
        }
    }

    @Override
    public void onRecordingStart(float fps, Size detectorSize) {
        this.detectorSize = detectorSize;
        float size = root.getResources().getDisplayMetrics().density * (60 + 60 + 36);
        xMult = size / detectorSize.width;
        yMult = size / detectorSize.height;
    }

    private void resetDetectionPosition() {
        if (detectProgressPos > 0 && swypeSequence != null && pathPointDrawable != null) {
            for (int i = 0; i < detectProgressPos; i++) {
                swypeSequence[i].setImageDrawable(pathPointDrawable);
            }
        }
        detectProgressPos = -1;

        currentPoint.setTranslationX(estimatePointCoord(sequenceIndices[0] % 3));
        currentPoint.setTranslationY(estimatePointCoord(sequenceIndices[0] / 3));
    }

    private int estimatePointCoord(int line) {
        return (int) (line * 96 * root.getResources().getDisplayMetrics().density);
    }

    private void loadDrawables() {
        if (empptyPointDrawable == null) {
            empptyPointDrawable = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_swype_empty, null);
            empptyPointDrawable.setBounds(0, 0, empptyPointDrawable.getIntrinsicWidth(), empptyPointDrawable.getIntrinsicHeight());
        }
        if (pathPointDrawable == null) {
            pathPointDrawable = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_swype_path, null);
            pathPointDrawable.setBounds(0, 0, pathPointDrawable.getIntrinsicWidth(), pathPointDrawable.getIntrinsicHeight());
        }
        if (visitedPointDrawable == null) {
            visitedPointDrawable = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_swype_visited, null);
            visitedPointDrawable.setBounds(0, 0, visitedPointDrawable.getIntrinsicWidth(), visitedPointDrawable.getIntrinsicHeight());
        }
    }
}
