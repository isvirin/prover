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
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Locale;

import io.prover.provermvp.R;
import io.prover.provermvp.camera.Size;
import io.prover.provermvp.permissions.PermissionManager;
import io.prover.provermvp.transport.NetworkHolder;
import io.prover.provermvp.transport.NetworkRequest;
import io.prover.provermvp.transport.RequestSwypeCode1;
import io.prover.provermvp.transport.responce.HelloResponce;
import io.prover.provermvp.transport.responce.LowFundsException;
import io.prover.provermvp.transport.responce.SwypeResponce2;
import io.prover.provermvp.util.Etherium;
import io.prover.provermvp.util.UtilFile;

/**
 * Created by babay on 07.11.2017.
 */

public class CameraControlsHolder implements View.OnClickListener, AdapterView.OnItemSelectedListener, NetworkRequest.NetworkRequestListener {
    private final ViewGroup root;
    private final ImageButton mainButton;
    private final Spinner resolutionSpinner;
    private final Activity activity;
    private final ICameraViewHolder cameraHolder;
    private final ArrayAdapter cameraResolutionsAdapter;
    private final TextView balanceView;
    private final SwypeStateHelperHolder swypeStateHelperHolder;
    boolean resumed = false;
    NetworkHolder networkHolder;
    boolean swypeConfirmed = true;
    private boolean started;

    public CameraControlsHolder(Activity activity, ViewGroup root, ImageButton mainButton, ICameraViewHolder cameraHolder, SwypeStateHelperHolder swypeStateHelperHolder) {
        cameraResolutionsAdapter = new ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item);
        cameraResolutionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.swypeStateHelperHolder = swypeStateHelperHolder;

        this.root = root;
        this.mainButton = mainButton;
        this.activity = activity;
        this.cameraHolder = cameraHolder;
        resolutionSpinner = root.findViewById(R.id.resolutionSpinner);
        balanceView = root.findViewById(R.id.balanceView);

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
                if (networkHolder != null && !cameraHolder.isRecording() && swypeStateHelperHolder.isVideoConfirmed()) {
                    networkHolder.onStopRecording(swypeConfirmed ? file : null);
                }
                swypeStateHelperHolder.setSwype(null);
            } else {
                PermissionManager.ensureHaveWriteSdcardPermission(activity, () -> {
                    if (started && cameraHolder.startRecording(activity)) {
                        updateMainButton(false, true);
                        if (networkHolder != null) {
                            boolean requesting = networkHolder.requestSwypeCode();
                            swypeStateHelperHolder.setSwypeStatus(requesting ? "requesting" : "not requesting");
                        }
                    }
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
        if (cameraHolder.isRecording()) {
            cameraHolder.cancelRecording();
            if (networkHolder != null)
                networkHolder.onStopRecording(null);
        }
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
        if (networkHolder == null) {
            Etherium etherium = Etherium.getInstance(activity);
            if (etherium.getKey() != null || etherium.getKey().equals(networkHolder.key)) {
                networkHolder = new NetworkHolder(etherium.getKey(), this);
            }
        }
        if (networkHolder != null)
            networkHolder.doHello();
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

    @Override
    public void onNetworkRequestDone(NetworkRequest request, Object responce) {
        if (responce instanceof HelloResponce) {
            String text = String.format(Locale.getDefault(), "balance: %.4f", ((HelloResponce) responce).getDoubleBalance());
            balanceView.setText(text);
        } else if (responce instanceof SwypeResponce2 && cameraHolder.isRecording()) {
            swypeStateHelperHolder.setSwype(((SwypeResponce2) responce).swypeCode);
        }
    }

    @Override
    public void onNetworkRequestError(NetworkRequest request, Exception e) {
        if (request instanceof RequestSwypeCode1) {
            if (e instanceof LowFundsException)
                swypeStateHelperHolder.setSwypeStatus("error: low funds");
            else
                swypeStateHelperHolder.setSwypeStatus("error");
        }
    }
}
