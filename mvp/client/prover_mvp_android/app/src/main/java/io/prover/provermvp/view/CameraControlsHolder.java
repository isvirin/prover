package io.prover.provermvp.view;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.io.File;

import io.prover.provermvp.R;
import io.prover.provermvp.permissions.PermissionManager;
import io.prover.provermvp.util.UtilFile;

/**
 * Created by babay on 07.11.2017.
 */

public class CameraControlsHolder implements View.OnClickListener {
    private final ViewGroup root;
    private final ImageButton mainButton;
    private final Activity activity;
    private final CameraViewHolder cameraHolder;
    boolean resumed = false;
    private boolean started;

    public CameraControlsHolder(Activity activity, ViewGroup root, ImageButton mainButton, CameraViewHolder cameraHolder) {
        this.root = root;
        this.mainButton = mainButton;
        this.activity = activity;
        this.cameraHolder = cameraHolder;

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
    }

    public void onStart() {
        started = true;
    }

    public void onStop() {
        started = false;
    }

}
