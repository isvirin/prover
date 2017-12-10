package io.prover.provermvp.viewholder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.Arrays;

import io.prover.provermvp.R;
import io.prover.provermvp.camera.Size;
import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.detector.DetectionState;

import static io.prover.provermvp.detector.DetectionState.State.InputCode;

/**
 * Created by babay on 22.11.2017.
 */

public class SwypeViewHolder implements CameraController.OnDetectionStateCahngedListener,
        CameraController.OnSwypeCodeSetListener, CameraController.OnRecordingStartListener, CameraController.OnRecordingStopListener {
    private final ConstraintLayout root;
    private final CameraController cameraController;
    private final SwipePointImageViewHolder[] swypePoints = new SwipePointImageViewHolder[9];
    //private final ImageView[] swypePoints = new ImageView[9];
    private final ImageView redPoint;
    private final Handler handler = new Handler();
    private final SwypeArrowHolder swypeArrowHolder;
    private final Matrix rotateScaleMatrix = new Matrix();
    private final Matrix pointMatrix = new Matrix();
    private final float[] point = new float[2];
    float xMult, yMult;
    private String swype;
    private SwipePointImageViewHolder[] swypeSequence;
    private int[] sequenceIndices;
    private boolean[] pointVisited;
    private int detectProgressPos;
    private VectorDrawableCompat emptyPointDrawable;

    private SwypeHolderState state = SwypeHolderState.Hidden;


    public SwypeViewHolder(ConstraintLayout root, CameraController cameraController) {
        this.root = root;
        this.cameraController = cameraController;

        swypePoints[0] = new SwipePointImageViewHolder(root.findViewById(R.id.swypePoint1));
        swypePoints[1] = new SwipePointImageViewHolder(root.findViewById(R.id.swypePoint2));
        swypePoints[2] = new SwipePointImageViewHolder(root.findViewById(R.id.swypePoint3));
        swypePoints[3] = new SwipePointImageViewHolder(root.findViewById(R.id.swypePoint4));
        swypePoints[4] = new SwipePointImageViewHolder(root.findViewById(R.id.swypePoint5));
        swypePoints[5] = new SwipePointImageViewHolder(root.findViewById(R.id.swypePoint6));
        swypePoints[6] = new SwipePointImageViewHolder(root.findViewById(R.id.swypePoint7));
        swypePoints[7] = new SwipePointImageViewHolder(root.findViewById(R.id.swypePoint8));
        swypePoints[8] = new SwipePointImageViewHolder(root.findViewById(R.id.swypePoint9));

        redPoint = root.findViewById(R.id.swypeCurrentPosition);
        swypeArrowHolder = new SwypeArrowHolder(root, swypePoints);

        cameraController.detectionState.add(this);
        cameraController.swypeCodeSet.add(this);
        cameraController.onRecordingStart.add(this);
        cameraController.onRecordingStop.add(this);
        root.setVisibility(View.GONE);
    }

    @Override
    public void onDetectionStateChanged(@Nullable DetectionState oldState, @NonNull DetectionState newState) {
        boolean shouldProcess = setState(newState.state);

        if (shouldProcess && swypeSequence != null) {

            int index = newState.index - 1;
            if (index >= swypeSequence.length)
                index = swypeSequence.length - 1;
            if (index >= 0 && !pointVisited[index]) {
                pointVisited[index] = true;
                swypeSequence[index].setState(SwipePointImageViewHolder.State.Visited);
                detectProgressPos = index;
                if (detectProgressPos < swypeSequence.length - 1) {
                    swypeArrowHolder.show(sequenceIndices[detectProgressPos], sequenceIndices[detectProgressPos + 1]);
                } else {
                    swypeArrowHolder.hide();
                }
                setRedPointPositionMatrixTo(swypeSequence[index].view);
                if (index + 1 < swypeSequence.length) {
                    swypeSequence[index + 1].setState(SwipePointImageViewHolder.State.Unvisited);
                }
            }
            point[0] = newState.x;
            point[1] = newState.y;
            pointMatrix.mapPoints(point);
            redPoint.setTranslationX(point[0]);
            redPoint.setTranslationY(point[1]);
        }
    }

    @Override
    public void onSwypeCodeSet(String swypeCode, String actualSwypeCode) {
        this.swype = swype;

        loadDrawables();
        swypeSequence = new SwipePointImageViewHolder[swypeCode.length()];
        sequenceIndices = new int[swypeCode.length()];
        pointVisited = new boolean[swypeCode.length()];
        char[] charArray = swypeCode.toCharArray();

        for (int i = 0; i < charArray.length; i++) {
            char ch = charArray[i];
            int pos = ch - '1';
            if (pos >= 0 && pos < 9) {
                swypeSequence[i] = swypePoints[pos];
                sequenceIndices[i] = pos;
            }
        }
        rotateScaleMatrix.reset();
        int orientation = cameraController.getOrientationHint();
        rotateScaleMatrix.postScale(1, 1);
        rotateScaleMatrix.postRotate(orientation, 0, 0);
        rotateScaleMatrix.postScale(xMult, yMult);
    }

    @Override
    public void onRecordingStart(float fps, Size detectorSize) {
        float size = root.getResources().getDisplayMetrics().density * 96;
        xMult = size / 1024.0f;
        yMult = size / 1024.0f;
    }

    private void resetDetectionPosition() {
        detectProgressPos = -1;

        redPoint.setTranslationX(estimatePointCoord(sequenceIndices[0] % 3));
        redPoint.setTranslationY(estimatePointCoord(sequenceIndices[0] / 3));
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
        cameraController.setSwypeDetectorPaused(true);
        int animationDuration = root.getResources().getInteger(R.integer.swypeBlinkAnimationDuration);
        Arrays.fill(pointVisited, false);
        swypeArrowHolder.hide();

        for (SwipePointImageViewHolder swypePoint : swypePoints) {
            swypePoint.setState(SwipePointImageViewHolder.State.None, emptyPointDrawable);
        }

        for (int i = 0; i < swypeSequence.length; i++) {
            final int pos = i;
            handler.postDelayed(() -> {
                if (swypeSequence == null)
                    return;
                swypeSequence[pos].setState(SwipePointImageViewHolder.State.Unvisited);
            }, animationDuration * i);
        }

        setRedPointPositionMatrixTo(swypeSequence[0].view);
        redPoint.setVisibility(View.GONE);
        handler.postDelayed(() -> {
            applyAnimatedVectorDrawable(redPoint, R.drawable.swype_track_point_blink);
            redPoint.setVisibility(View.VISIBLE);
            redPoint.bringToFront();
            swypeSequence[0].setState(SwipePointImageViewHolder.State.Visited);
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
        /*if (swypeSequence != null) {
            for (SwipePointImageViewHolder swypePoint : swypePoints) {
                swypePoint.setState(SwipePointImageViewHolder.State.None, emptyPointDrawable);
            }
        }*/
    }

    private void setRedPointPositionMatrixTo(View v) {
        pointMatrix.set(rotateScaleMatrix);
        int dx = redPoint.getWidth() / 2;
        int dy = redPoint.getHeight() / 2;
        if (dx == 0 || dy == 0) {
            Drawable dr = redPoint.getDrawable();
            dx = dr.getIntrinsicWidth() / 2;
            dy = dr.getIntrinsicHeight() / 2;
        }
        pointMatrix.postTranslate(-dx, -dy);
        pointMatrix.postTranslate((v.getLeft() + v.getRight()) / 2, (v.getTop() + v.getBottom()) / 2);
    }

    @Override
    public void onRecordingStop(File file, boolean isVideoConfirmed) {
        hide();
        swypeSequence = null;
    }

    private void showFailed() {
        for (SwipePointImageViewHolder swypePoint : swypePoints) {
            swypePoint.setState(SwipePointImageViewHolder.State.Failed);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(root);
        }
        redPoint.setVisibility(View.GONE);
        swypeArrowHolder.hide();

        handler.postDelayed(() -> {
            if (state == SwypeHolderState.Failed) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    TransitionManager.beginDelayedTransition(root);
                }
                hide();
            }
        }, 1500);
    }

    private void showCompleted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition((ViewGroup) root.getParent());
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(root, "alpha", 1, 0);
        animator.setDuration(400);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                root.setVisibility(View.GONE);
                root.setAlpha(1);
            }
        });
        animator.start();
    }

    /**
     * @param state
     * @return true if state should be processed
     */
    public boolean setState(DetectionState.State state) {
        SwypeHolderState oldState = this.state;
        this.state = SwypeHolderState.ofDetectorState(state, this.state);
        if (this.state == SwypeHolderState.Showing && swypeSequence == null) {
            this.state = SwypeHolderState.Hidden;
        }

        if (this.state == oldState)
            return this.state == SwypeHolderState.Showing;

        boolean visible = root.getVisibility() == View.VISIBLE;

        switch (this.state) {
            case Hidden:
                if (visible)
                    hide();
                return false;

            case Showing:
                if (!visible)
                    show();
                return state == InputCode;

            case Completed:
                showCompleted();
                return true;

            case Failed:
                showFailed();
                return false;

        }
        return false;
    }

    private enum SwypeHolderState {
        Hidden, Showing, Failed, Completed;

        public static SwypeHolderState ofDetectorState(DetectionState.State state, SwypeHolderState current) {
            switch (state) {
                case Waiting:
                case GotProverNoCode:
                    switch (current) {
                        case Failed:
                        case Showing:
                            return Failed;

                        case Completed:
                            return Completed;

                        case Hidden:
                            return Hidden;
                    }
                    return Hidden;

                case GotProverWaiting:
                case InputCode:
                    return Showing;

                case Confirmed:
                    return Completed;
            }
            return Hidden;
        }
    }
}
