package io.prover.provermvp.viewholder;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.Image;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
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
import io.prover.provermvp.permissions.PermissionManager;
import io.prover.provermvp.transport.NetworkHolder;
import io.prover.provermvp.transport.NetworkRequest;
import io.prover.provermvp.transport.responce.HelloResponce;
import io.prover.provermvp.util.Etherium;
import io.prover.provermvp.util.FrameRateCounter;
import io.prover.provermvp.util.UtilFile;

import static io.prover.provermvp.Const.KEY_SELECTED_RESOLUTION_X;
import static io.prover.provermvp.Const.KEY_SELECTED_RESOLUTION_Y;

/**
 * Created by babay on 07.11.2017.
 */

public class CameraControlsHolder implements View.OnClickListener,
        AdapterView.OnItemSelectedListener,
        CameraController.OnPreviewStartListener,
        CameraController.OnFrameAvailableListener,
        CameraController.OnFrameAvailable2Listener,
        CameraController.OnRecordingStartListener,
        CameraController.NetworkRequestDoneListener, CameraController.OnRecordingStopListener {
    private final ViewGroup root;
    private final ImageButton mainButton;
    private final Spinner resolutionSpinner;
    private final Activity activity;
    private final ICameraViewHolder cameraHolder;
    private final ArrayAdapter<Size> cameraResolutionsAdapter;
    private final TextView balanceView;
    private final CameraController cameraController;
    private final FrameRateCounter fpsCounter = new FrameRateCounter(60, 10);
    private final TextView fpsView;
    boolean resumed = false;
    NetworkHolder networkHolder;
    private boolean started;

    public CameraControlsHolder(Activity activity, ViewGroup root, ImageButton mainButton, ICameraViewHolder cameraHolder, CameraController cameraController) {
        cameraResolutionsAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item);
        cameraResolutionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        this.root = root;
        this.mainButton = mainButton;
        this.activity = activity;
        this.cameraHolder = cameraHolder;
        this.cameraController = cameraController;

        resolutionSpinner = root.findViewById(R.id.resolutionSpinner);
        balanceView = root.findViewById(R.id.balanceView);
        fpsView = root.findViewById(R.id.fpsCounter);

        resolutionSpinner.setAdapter(cameraResolutionsAdapter);
        resolutionSpinner.setOnItemSelectedListener(this);

        mainButton.setOnClickListener(this);

        fpsView.bringToFront();
        cameraController.previewStart.add(this);
        cameraController.frameAvailable.add(this);
        cameraController.frameAvailable2.add(this);
        cameraController.onRecordingStart.add(this);
        cameraController.onRecordingStop.add(this);
        cameraController.networkRequestDone.add(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(root.getContext());
        Size resolution = Size.fromPreferences(prefs, KEY_SELECTED_RESOLUTION_X, KEY_SELECTED_RESOLUTION_Y);
        if (resolution != null)
            cameraHolder.setCameraResolution(resolution);
    }

    @Override
    public void onClick(View v) {
        if (v == mainButton) {
            if (cameraController.isRecording()) {
                cameraHolder.finishRecording();

            } else {
                PermissionManager.ensureHaveWriteSdcardPermission(activity, () ->
                        cameraController.handler.postDelayed(() -> {
                            if (started)
                                cameraHolder.startRecording(activity, fpsCounter.getAvgFps());
                        }, 100));
            }
        }
    }

    private void updateControls(boolean wasPlaying, boolean playing) {
        if (wasPlaying == playing)
            return;
        VectorDrawableCompat dr;
        if (playing) {
            dr = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_stop_24dp, null);
        } else {
            dr = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_record_24dp, null);
        }
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        mainButton.setImageDrawable(dr);

        resolutionSpinner.setEnabled(!playing);
    }

    public void onPause() {
        resumed = false;
    }

    public void onResume() {
        resumed = true;

        if (networkHolder == null) {
            Etherium etherium = Etherium.getInstance(activity);
            if (etherium.getKey() != null || !etherium.getKey().equals(networkHolder.key)) {
                networkHolder = new NetworkHolder(etherium.getKey(), cameraController);
            }
        }
        if (networkHolder != null)
            networkHolder.doHello();
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
    public void onNetworkRequestDone(NetworkRequest request, Object responce) {
        if (responce instanceof HelloResponce) {
            String text = String.format(Locale.getDefault(), "balance: %.4f", ((HelloResponce) responce).getDoubleBalance());
            balanceView.setText(text);
        }
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
    public void onFrameAvailable(byte[] data, Camera camera) {
        float fps = fpsCounter.addFrame();
        if (fps >= 0) {
            fpsView.setText(String.format(Locale.getDefault(), "%.1f", fps));
        }
    }

    @Override
    public void onFrameAvailable(Image image) {
        float fps = fpsCounter.addFrame();
        if (fps >= 0) {
            fpsView.setText(String.format(Locale.getDefault(), "%.1f", fps));
        }
    }

    @Override
    public void onRecordingStart(float fps) {
        updateControls(false, true);
    }

    @Override
    public void onRecordingStop(File file, boolean isVideoConfirmed) {
        if (file != null) {
            Snackbar.make(root, "Finished file: " + file.getPath(), Snackbar.LENGTH_LONG)
                    .setAction("Open", v1 -> new UtilFile(file).externalOpenFile(root.getContext(), null))
                    .show();
        }
        updateControls(true, false);
    }
}
