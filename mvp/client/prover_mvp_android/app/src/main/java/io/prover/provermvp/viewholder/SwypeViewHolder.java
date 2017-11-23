package io.prover.provermvp.viewholder;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.Arrays;

import io.prover.provermvp.R;
import io.prover.provermvp.camera.Size;
import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.detector.DetectionState;

/**
 * Created by babay on 22.11.2017.
 */

public class SwypeViewHolder implements CameraController.OnDetectionStateCahngedListener,
        CameraController.OnSwypeCodeSetListener, CameraController.OnRecordingStartListener, CameraController.OnRecordingStopListener {
    private final ConstraintLayout root;
    private final CameraController cameraController;
    private final ImageView[] swypePoints = new ImageView[9];
    private final ImageView currentPoint;
    private final Handler handler = new Handler();
    private final SwypeArrowHolder swypeArrowHolder;
    float xMult, yMult;
    private String swype;
    private ImageView[] swypeSequence;
    private int[] sequenceIndices;
    private boolean[] pointVisited;
    private int detectProgressPos;
    private VectorDrawableCompat emptyPointDrawable;
    private boolean detectionPaused = false;


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
        swypeArrowHolder = new SwypeArrowHolder(root, swypePoints);

        cameraController.detectionState.add(this);
        cameraController.swypeCodeSet.add(this);
        cameraController.onRecordingStart.add(this);
        cameraController.onRecordingStop.add(this);
        root.setVisibility(View.GONE);
    }

    @Override
    public void onDetectionStateChanged(@Nullable DetectionState oldState, @NonNull DetectionState newState) {
        if (detectionPaused)
            return;
        boolean visible = root.getVisibility() == View.VISIBLE;
        boolean shouldBeVisible = newState.state == 2;
        if (visible != shouldBeVisible) {
            if (shouldBeVisible)
                show();
            else
                hide();
            return;
        }

        if (!shouldBeVisible || swypeSequence == null)
            return;

        if (newState.x != 0 || newState.y != 0 || newState.index != 0) {
            currentPoint.setTranslationX(newState.x * xMult);
            currentPoint.setTranslationY(newState.y * yMult);
        }
        int index = tempGetActualIndex(newState);
        if (!pointVisited[index]) {
            applyAnimatedVectorDrawable(swypeSequence[index], R.drawable.swype_path_point_fill);
            detectProgressPos = index;
            if (detectProgressPos < swypeSequence.length - 1) {
                swypeArrowHolder.show(sequenceIndices[detectProgressPos], sequenceIndices[detectProgressPos + 1]);
            } else {
                swypeArrowHolder.hide();
            }
        }
    }

    private int tempGetActualIndex(DetectionState state) {
        if (state.index == 0)
            return 0;
        int index = state.index - 1;
        int pos = detectProgressPos < 0 ? 0 : detectProgressPos;
        for (int i = pos; i < sequenceIndices.length; i++) {
            if (sequenceIndices[i] == index)
                return i;
        }
        return 0;
    }

    @Override
    public void onSwypeCodeSet(String swypeCode) {
        this.swype = swype;

        loadDrawables();
        swypeSequence = new ImageView[swypeCode.length()];
        sequenceIndices = new int[swypeCode.length()];
        pointVisited = new boolean[swypeCode.length()];
        char[] charArray = swypeCode.toCharArray();

        for (ImageView swypePoint : swypePoints) {
            swypePoint.setImageDrawable(emptyPointDrawable);
        }
        for (int i = 0; i < charArray.length; i++) {
            char ch = charArray[i];
            int pos = ch - '1';
            if (pos >= 0 && pos < 9) {
                swypeSequence[i] = swypePoints[pos];
                sequenceIndices[i] = pos;
            }
        }
    }

    @Override
    public void onRecordingStart(float fps, Size detectorSize) {
        float size = root.getResources().getDisplayMetrics().density * (60 + 60 + 36);
        xMult = size / detectorSize.width;
        yMult = size / detectorSize.height;
    }

    private void resetDetectionPosition() {
        detectProgressPos = -1;

        currentPoint.setTranslationX(estimatePointCoord(sequenceIndices[0] % 3));
        currentPoint.setTranslationY(estimatePointCoord(sequenceIndices[0] / 3));
    }

    private int estimatePointCoord(int line) {
        return (int) (line * 96 * root.getResources().getDisplayMetrics().density);
    }

    private void loadDrawables() {
        if (emptyPointDrawable == null) {
            emptyPointDrawable = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_swype_empty, null);
            emptyPointDrawable.setBounds(0, 0, emptyPointDrawable.getIntrinsicWidth(), emptyPointDrawable.getIntrinsicHeight());
        }
    }

    private void show() {
        root.setVisibility(View.VISIBLE);
        resetDetectionPosition();
        detectionPaused = true;
        cameraController.setSwypeDetectorPaused(true);
        int animationDuration = root.getResources().getInteger(R.integer.swypeBlinkAnimationDuration);
        Arrays.fill(pointVisited, false);
        swypeArrowHolder.hide();

        for (int i = 0; i < swypeSequence.length; i++) {
            final int pos = i;
            handler.postDelayed(() -> {
                if (swypeSequence == null)
                    return;
                applyAnimatedVectorDrawable(swypeSequence[pos], R.drawable.swype_path_point_blink);
            }, animationDuration * i);
        }

        applyAnimatedVectorDrawable(currentPoint, R.drawable.swype_track_point_blink);
        handler.postDelayed(() -> {
            detectionPaused = false;
            cameraController.setSwypeDetectorPaused(false);
        }, animationDuration * (swypeSequence.length + 1));
    }

    private void applyAnimatedVectorDrawable(ImageView view, int id) {
        AnimatedVectorDrawableCompat dr = AnimatedVectorDrawableCompat.create(root.getContext(), id);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        view.setImageDrawable(dr);
        dr.start();
    }

    private void hide() {
        root.setVisibility(View.GONE);
        if (swypeSequence != null) {
            for (ImageView imageView : swypeSequence) {
                imageView.setImageDrawable(emptyPointDrawable);
            }
        }
    }

    @Override
    public void onRecordingStop(File file, boolean isVideoConfirmed) {
        hide();
        swypeSequence = null;
    }
}
