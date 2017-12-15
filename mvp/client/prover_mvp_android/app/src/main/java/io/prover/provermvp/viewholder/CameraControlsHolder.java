package io.prover.provermvp.viewholder;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.transition.TransitionManager;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Locale;

import io.prover.provermvp.R;
import io.prover.provermvp.camera.Size;
import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.detector.DetectionState;
import io.prover.provermvp.permissions.PermissionManager;
import io.prover.provermvp.transport.HelloRequest;
import io.prover.provermvp.transport.NetworkRequest;
import io.prover.provermvp.transport.SubmitVideoHashRequest;
import io.prover.provermvp.transport.responce.HelloResponce;
import io.prover.provermvp.util.UtilFile;

import static io.prover.provermvp.Const.KEY_SELECTED_RESOLUTION_X;
import static io.prover.provermvp.Const.KEY_SELECTED_RESOLUTION_Y;

/**
 * Created by babay on 07.11.2017.
 */

public class CameraControlsHolder implements View.OnClickListener,
        AdapterView.OnItemSelectedListener,
        CameraController.OnPreviewStartListener,
        CameraController.OnRecordingStartListener, CameraController.OnRecordingStopListener, CameraController.OnSwypeCodeSetListener, CameraController.OnFpsUpdateListener, CameraController.OnDetectionStateCahngedListener, CameraController.NetworkRequestDoneListener, CameraController.NetworkRequestErrorListener, CameraController.NetworkRequestStartListener {
    private final ViewGroup root;
    private final Spinner resolutionSpinner;
    private final Activity activity;
    private final ICameraViewHolder cameraHolder;
    private final ArrayAdapter<Size> cameraResolutionsAdapter;
    private final CameraController cameraController;
    private final TextView fpsView;
    private final ImageButton recordButton;
    private final AppCompatImageView largeImageNotification;
    private final TextView hintText;
    private final Handler handler = new Handler();
    private final AllDoneImageHolder allDoneHolder;
    private final SwypeViewHolder swypeViewHolder;
    private boolean started;

    public CameraControlsHolder(Activity activity, ViewGroup root, ICameraViewHolder cameraHolder, CameraController cameraController) {
        cameraResolutionsAdapter = new ArrayAdapter<>(activity, R.layout.simple_spinner_item_nopad);
        cameraResolutionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        this.root = root;
        this.activity = activity;
        this.cameraHolder = cameraHolder;
        this.cameraController = cameraController;

        resolutionSpinner = root.findViewById(R.id.resolutionSpinner);
        fpsView = root.findViewById(R.id.fpsCounter);
        recordButton = root.findViewById(R.id.recordButton);
        largeImageNotification = root.findViewById(R.id.largeImageNotification);
        hintText = root.findViewById(R.id.hintText);
        allDoneHolder = new AllDoneImageHolder(root.findViewById(R.id.allDoneIcon));

        resolutionSpinner.setAdapter(cameraResolutionsAdapter);
        resolutionSpinner.setOnItemSelectedListener(this);

        recordButton.setOnClickListener(this);

        fpsView.bringToFront();
        cameraController.previewStart.add(this);
        cameraController.onRecordingStart.add(this);
        cameraController.onRecordingStop.add(this);
        cameraController.swypeCodeSet.add(this);
        cameraController.fpsUpdateListener.add(this);
        cameraController.detectionState.add(this);
        cameraController.onNetworkRequestDone.add(this);
        cameraController.onNetworkRequestError.add(this);
        cameraController.onNetworkRequestStart.add(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(root.getContext());
        Size resolution = Size.fromPreferences(prefs, KEY_SELECTED_RESOLUTION_X, KEY_SELECTED_RESOLUTION_Y);
        if (resolution != null)
            cameraHolder.setCameraResolution(resolution);
        swypeViewHolder = new SwypeViewHolder(root.findViewById(R.id.swypeView), cameraController);
    }

    private static Animatable2Compat.AnimationCallback animationCallbackOfRunnables(Runnable onStart, Runnable onEnd) {
        return new Animatable2Compat.AnimationCallback() {
            @Override
            public void onAnimationStart(Drawable drawable) {
                onStart.run();
            }

            @Override
            public void onAnimationEnd(Drawable drawable) {
                onEnd.run();
            }
        };
    }

    @Override
    public void onClick(View v) {
        if (v == recordButton) {
            if (cameraController.isRecording()) {
                updateControls(true, false);
                cameraHolder.finishRecording();
            } else {
                if (PermissionManager.checkHaveWriteSdcardPermission(activity)) {
                    cameraHolder.startRecording(activity);
                    updateControls(false, true);
                } else {
                    PermissionManager.ensureHaveWriteSdcardPermission(activity, () ->
                            cameraController.handler.postDelayed(() -> {
                                if (started) {
                                    cameraHolder.startRecording(activity);
                                    updateControls(false, true);
                                }
                            }, 500));
                }
            }
        }
    }

    private void updateControls(boolean wasPlaying, boolean playing) {
        if (wasPlaying == playing)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AnimatedVectorDrawable adr;
            if (playing) {
                adr = (AnimatedVectorDrawable) root.getResources().getDrawable(R.drawable.ic_record_start_animated);
            } else {
                adr = (AnimatedVectorDrawable) root.getResources().getDrawable(R.drawable.ic_record_stop_animated);
            }
            adr.setBounds(0, 0, adr.getIntrinsicWidth(), adr.getIntrinsicHeight());
            recordButton.setImageDrawable(adr);
            adr.start();
        } else {
            Drawable dr;
            if (playing) {
                dr = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_stop_record_icon, null);
            } else {
                dr = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_start_record_icon, null);
            }
            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
            recordButton.setImageDrawable(dr);
        }

        resolutionSpinner.setEnabled(!playing);
    }

    public void onStart() {
        started = true;
        updateControls(true, false);
    }

    public void onStop() {
        started = false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        cameraHolder.setCameraResolution((Size) parent.getItemAtPosition(position));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onPreviewStart(@NonNull List<Size> sizes, @NonNull Size previewSize) {
        cameraResolutionsAdapter.clear();
        cameraResolutionsAdapter.addAll(sizes);
        int pos = sizes.indexOf(previewSize);
        if (pos >= 0)
            resolutionSpinner.setSelection(pos, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(root.getContext());
        Size resolution = Size.fromPreferences(prefs, KEY_SELECTED_RESOLUTION_X, KEY_SELECTED_RESOLUTION_Y);
        if (!previewSize.equalsIgnoringRotation(resolution)) {
            SharedPreferences.Editor editor = prefs.edit();
            previewSize.saveToPreferences(editor, KEY_SELECTED_RESOLUTION_X, KEY_SELECTED_RESOLUTION_Y);
            editor.apply();
        }
    }

    @Override
    public void onRecordingStart() {
        //updateControls(false, true);
    }

    @Override
    public void onRecordingStop(File file, boolean isVideoConfirmed) {
        if (file != null) {
            Snackbar.make(root, "Finished file: " + file.getPath(), Snackbar.LENGTH_LONG)
                    .setAction("Open", v1 -> new UtilFile(file).externalOpenFile(root.getContext(), null))
                    .show();
        }
        updateControls(true, false);
        allDoneHolder.setVectorDrawable();
        allDoneHolder.hide();
        if (file != null && !isVideoConfirmed) {
            TransitionManager.beginDelayedTransition(root);
            showImageNotificationAnim(R.drawable.ic_not_verified_anim, 3000, false);
            showHint(R.string.videoNotConfirmed, 4000, 0, false, false);
        }
    }

    @Override
    public void onSwypeCodeSet(String swypeCode, String actualSwypeCode) {
        if (actualSwypeCode == null) {
            TransitionManager.beginDelayedTransition(root);
            showImageNotificationAnim(R.drawable.phone_large_animated1_fadeout, 2000, false);
            showHint(R.string.makeProver, 5000, 0, false, false);
        }
    }

    @Override
    public void OnFpsUpdate(float fps, float processorFps) {
        fpsView.setText(String.format(Locale.getDefault(), "%.1f/%.1f fps ", fps, processorFps));
    }

    @Override
    public void onDetectionStateChanged(@Nullable DetectionState oldState, @NonNull DetectionState newState) {
        if (oldState != null && oldState.state == DetectionState.State.InputCode && newState.state == DetectionState.State.Waiting) {
            showHint(R.string.swipeCodeFailedTryAgain, 3500, 0, false, true);
        } else if (newState.state == DetectionState.State.Confirmed) {
            allDoneHolder.setVectorDrawable();
            TransitionManager.beginDelayedTransition(root);
            allDoneHolder.show();
            swypeViewHolder.hide();
            showHint(R.string.swypeCodeOk, 3500, 0, false, false);
            handler.postDelayed(allDoneHolder::animateMove, 1000);
        }
    }

    private void showHint(int stringId, long timeoutToHide, int anchor, boolean isAbove, boolean animate) {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) hintText.getLayoutParams();
        if (anchor == 0) {
            lp.topToBottom = R.id.balanceContainer;
            lp.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
        } else if (isAbove) {
            lp.topToBottom = ConstraintLayout.LayoutParams.UNSET;
            lp.bottomToTop = anchor;
        } else {
            lp.topToBottom = anchor;
            lp.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
        }
        hintText.setLayoutParams(lp);

        if (animate)
            TransitionManager.beginDelayedTransition(root);

        hintText.setText(stringId);
        hintText.setVisibility(View.VISIBLE);

        if (timeoutToHide > 0)
            handler.postDelayed(() -> {
                TransitionManager.beginDelayedTransition(root);
                hintText.setVisibility(View.GONE);
            }, timeoutToHide);
    }

    private void showImageNotificationAnim(int vectorDrawableId, long timeout, boolean animateAppear) {
        TransitionManager.beginDelayedTransition(root);
        AnimatedVectorDrawableCompat dr = AnimatedVectorDrawableCompat.create(root.getContext(), vectorDrawableId);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        largeImageNotification.setImageDrawable(dr);
        largeImageNotification.setVisibility(View.VISIBLE);

        Runnable hide = () -> {
            TransitionManager.beginDelayedTransition(root);
            largeImageNotification.setVisibility(View.GONE);
        };

        if (timeout == 0) {
            dr.registerAnimationCallback(animationCallbackOfRunnables(null, hide));
        } else {
            handler.postDelayed(hide, timeout);
        }
        dr.start();
    }

    @Override
    public void onNetworkRequestDone(NetworkRequest request, Object responce) {
        if (request instanceof SubmitVideoHashRequest) {
            showHint(R.string.videoHashPosted, 3000, 0, false, true);
        } else if (request instanceof HelloRequest) {
            HelloResponce hello = (HelloResponce) responce;
            if (hello.getDoubleBalance() == 0) {
                TransitionManager.beginDelayedTransition(root);
                showImageNotificationAnim(R.drawable.no_money_anim, 3000, false);
                showHint(R.string.notEnoughMoney, 3500, 0, false, false);
            }
        }
    }

    @Override
    public void onNetworkRequestError(NetworkRequest request, Exception e) {
        if (request instanceof SubmitVideoHashRequest) {
            showHint(R.string.videoHashPosted, 3000, 0, false, true);
        }
    }

    @Override
    public void onNetworkRequestStart(NetworkRequest request) {
        if (request instanceof SubmitVideoHashRequest) {
            showHint(R.string.calculatingVideoHash, 4000, 0, false, true);
        }
    }
}
