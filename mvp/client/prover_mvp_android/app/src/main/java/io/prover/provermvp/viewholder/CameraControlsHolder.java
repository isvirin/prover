package io.prover.provermvp.viewholder;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.io.File;
import java.util.List;

import io.prover.provermvp.R;
import io.prover.provermvp.camera.Size;
import io.prover.provermvp.permissions.PermissionManager;
import io.prover.provermvp.util.UtilFile;

/**
 * Created by babay on 07.11.2017.
 */

public class CameraControlsHolder implements View.OnClickListener, AdapterView.OnItemSelectedListener {


    private final ViewGroup root;
    private final ImageButton mainButton;
    private final Spinner resolutionSpinner;
    private final Activity activity;
    private final ICameraViewHolder cameraHolder;
    private final ArrayAdapter cameraResolutionsAdapter;
    boolean resumed = false;
    private boolean started;

    public CameraControlsHolder(Activity activity, ViewGroup root, ImageButton mainButton, ICameraViewHolder cameraHolder) {
        cameraResolutionsAdapter = new ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item);
        cameraResolutionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        this.root = root;
        this.mainButton = mainButton;
        this.activity = activity;
        this.cameraHolder = cameraHolder;
        resolutionSpinner = root.findViewById(R.id.resolutionSpinner);
        resolutionSpinner.setAdapter(cameraResolutionsAdapter);
        resolutionSpinner.setOnItemSelectedListener(this);

        mainButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mainButton) {
            if (cameraHolder.isRecording()) {
                cameraHolder.finishRecording();
                final File file = cameraHolder.getVideoFile();
                if (file != null) {
                    Snackbar.make(v, "Finished file: " + file.getPath(), Snackbar.LENGTH_LONG)
                            .setAction("Open", v1 -> new UtilFile(file).externalOpenFile(root.getContext(), null))
                            .show();
                }
                updateMainButton(true, cameraHolder.isRecording());
            } else {
                PermissionManager.ensureHaveWriteSdcardPermission(activity, () -> {
                    if (started && cameraHolder.startRecording(activity))
                        updateMainButton(false, true);
                });
            }
        }
    }

    private void updateMainButton(boolean wasPlaying, boolean playing) {
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
    }

    public void onPause() {
        resumed = false;
    }

    public void onResume() {
        resumed = true;

        List<Size> cameraResolutions = cameraHolder.getCameraResolutions();
        cameraResolutionsAdapter.clear();
        if (cameraResolutions != null)
            cameraResolutionsAdapter.addAll(cameraResolutions);
        Size selectedResolution = cameraHolder.getSelectedCameraResolution();
        if (cameraResolutions != null && selectedResolution != null) {
            int pos = cameraResolutions.indexOf(selectedResolution);
            if (pos >= 0)
                resolutionSpinner.setSelection(pos, false);
        }
    }

    public void onStart() {
        started = true;
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
}
