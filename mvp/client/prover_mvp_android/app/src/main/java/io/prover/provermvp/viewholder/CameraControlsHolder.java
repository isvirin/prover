package io.prover.provermvp.viewholder;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.Image;
import android.os.Build;
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
        CameraController.OnRecordingStartListener, CameraController.OnRecordingStopListener {
    private final ViewGroup root;
    private final Spinner resolutionSpinner;
    private final Activity activity;
    private final ICameraViewHolder cameraHolder;
    private final ArrayAdapter<Size> cameraResolutionsAdapter;
    private final CameraController cameraController;
    private final FrameRateCounter fpsCounter = new FrameRateCounter(60, 10);
    private final TextView fpsView;
    private final ImageButton recordButton;
    boolean resumed = false;
    NetworkHolder networkHolder;
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

        resolutionSpinner.setAdapter(cameraResolutionsAdapter);
        resolutionSpinner.setOnItemSelectedListener(this);

        recordButton.setOnClickListener(this);

        fpsView.bringToFront();
        cameraController.previewStart.add(this);
        cameraController.frameAvailable.add(this);
        cameraController.frameAvailable2.add(this);
        cameraController.onRecordingStart.add(this);
        cameraController.onRecordingStop.add(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(root.getContext());
        Size resolution = Size.fromPreferences(prefs, KEY_SELECTED_RESOLUTION_X, KEY_SELECTED_RESOLUTION_Y);
        if (resolution != null)
            cameraHolder.setCameraResolution(resolution);
    }

    @Override
    public void onClick(View v) {
        if (v == recordButton) {
            if (cameraController.isRecording()) {
                updateControls(true, false);
                cameraHolder.finishRecording();
            } else {
                if (PermissionManager.checkHaveWriteSdcardPermission(activity)) {
                    cameraHolder.startRecording(activity, fpsCounter.getAvgFps());
                    updateControls(false, true);
                } else {
                    PermissionManager.ensureHaveWriteSdcardPermission(activity, () ->
                            cameraController.handler.postDelayed(() -> {
                                if (started) {
                                    cameraHolder.startRecording(activity, fpsCounter.getAvgFps());
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
            fpsView.setText(String.format(Locale.getDefault(), "%.1f fps", fps));
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onFrameAvailable(Image image) {
        float fps = fpsCounter.addFrame();
        if (fps >= 0) {
            if (cameraController.isRecording()) {
                fpsView.setText(String.format(Locale.getDefault(), "%.1f/%.1f fps ", fps, cameraController.getDetectorFps()));
            } else {
                try {
                    fpsView.setText(String.format(Locale.getDefault(), "%.1f fps 0x%x %dx%d", fps, image.getFormat(), image.getWidth(), image.getHeight()));
                } catch (Exception e){
                    fpsView.setText(String.format(Locale.getDefault(), "%.1f/%.1f fps ", fps, cameraController.getDetectorFps()));
                }
            }
        }
    }

    @Override
    public void onRecordingStart(float fps, Size detectorSize) {
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
    }
}