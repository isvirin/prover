package io.prover.provermvp.viewholder;

import android.hardware.Camera;
import android.media.Image;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.SpannableStringBuilder;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

import io.prover.provermvp.R;
import io.prover.provermvp.Settings;
import io.prover.provermvp.camera.Size;
import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.detector.DetectionState;
import io.prover.provermvp.detector.SwypeDetectorHandler;
import io.prover.provermvp.transport.NetworkRequest;
import io.prover.provermvp.transport.RequestSwypeCode1;
import io.prover.provermvp.transport.responce.LowFundsException;

/**
 * Created by babay on 11.11.2017.
 */

public class SwypeStateHelperHolder implements
        CameraController.OnFrameAvailableListener,
        CameraController.OnFrameAvailable2Listener,
        CameraController.NetworkRequestErrorListener,
        CameraController.OnRecordingStartListener,
        CameraController.OnRecordingStopListener,
        CameraController.NetworkRequestStartListener,
        CameraController.OnDetectionStateCahngedListener, CameraController.OnSwypeCodeSetListener {
    private final ViewGroup root;
    private final TextView statsText;
    private final CameraController cameraController;
    int latestState = 0;
    private String swype;
    private String swypeStatus;
    private SwypeDetectorHandler detectorHandler;

    public SwypeStateHelperHolder(ViewGroup root, CameraController cameraController) {
        this.root = root;
        statsText = root.findViewById(R.id.statsView);
        this.cameraController = cameraController;
        statsText.bringToFront();
        cameraController.setSwypeStateHelperHolder(this);
        cameraController.frameAvailable.add(this);
        cameraController.frameAvailable2.add(this);
        cameraController.onNetworkRequestStart.add(this);
        cameraController.onNetworkRequestError.add(this);
        cameraController.onRecordingStart.add(this);
        cameraController.onRecordingStop.add(this);
        cameraController.detectionState.add(this);
        cameraController.swypeCodeSet.add(this);
    }

    @Override
    public void onDetectionStateChanged(@Nullable DetectionState oldState, @NonNull DetectionState state) {
        String stateStr = String.format(Locale.getDefault(), "%d, %d, %d, %d", state.state, state.index, state.x, state.y);
        setStatusText(stateStr);
        latestState = state.state;
        if (state.state == 1 && swype != null && detectorHandler != null) {
            detectorHandler.sendSetSwype(swype);
        }
    }

    private void setStatusText(String stateText) {
        if (swypeStatus == null)
            statsText.setText(stateText);
        else {
            CharSequence chars = statsText.getText();
            SpannableStringBuilder builder = chars instanceof SpannableStringBuilder ? (SpannableStringBuilder) chars : new SpannableStringBuilder();
            builder.clearSpans();
            builder.clear();
            builder.append(stateText).append("\n");
            builder.append(swypeStatus);
            statsText.setText(builder);
        }
    }

    public void setSwype(String swype) {
        this.swype = swype;
        this.swypeStatus = swype;
        if (swype == null) {
            statsText.setText("0");
        } else {
            String str = statsText.getText().toString();
            int pos = str.indexOf('\n');
            if (pos > 0) {
                str = str.substring(0, pos);
            }
            setStatusText(str);
            if (detectorHandler != null)
                detectorHandler.sendSetSwype(swype);
        }
    }

    public void setSwypeStatus(String swypeStatus) {
        this.swypeStatus = swypeStatus;
        String str = statsText.getText().toString();
        int pos = str.indexOf('\n');
        if (pos > 0) {
            str = str.substring(0, pos);
        }
        setStatusText(str);
    }

    public boolean isVideoConfirmed() {
        return latestState == 3;
    }

    @Override
    public void onRecordingStart(float fps, Size detectorSize) {
        detectorHandler = SwypeDetectorHandler.newHandler((int) fps * 2, swype, cameraController);
        setSwypeStatus("not requesting");
    }

    @Override
    public void onRecordingStop(File file, boolean isVideoConfirmed) {
        setSwype(null);
        if (detectorHandler != null) {
            detectorHandler.sendQuit();
            detectorHandler = null;
        }
    }

    @Override
    public void onFrameAvailable(byte[] data, Camera camera) {
        if (detectorHandler != null) {
            Camera.Parameters params = camera.getParameters();
            Camera.Size size = params.getPreviewSize();
            if (!detectorHandler.sendProcesstFrame(data, size.width, size.height)) {
                cameraController.frameReleased.postNotifyEvent(data);
            }
        } else {
            cameraController.frameReleased.postNotifyEvent(data);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onFrameAvailable(Image image) {
        if (detectorHandler != null) {
            if (!detectorHandler.sendProcesstFrame(image)) {
                image.close();
            }
        } else {
            image.close();
        }
    }

    @Override
    public void onNetworkRequestStart(NetworkRequest request) {
        if (request instanceof RequestSwypeCode1) {
            setSwypeStatus("requesting");
        }
    }

    @Override
    public void onNetworkRequestError(NetworkRequest request, Exception e) {
        if (request instanceof RequestSwypeCode1) {
            if (e instanceof LowFundsException) {
                if (Settings.FAKE_SWYPE_CODE) {
                    setSwypeStatus("error: low funds;\nexpecting fake code");
                } else {
                    setSwypeStatus("error: low funds");
                }
            } else
                setSwypeStatus("error");
        }
    }

    @Override
    public void onSwypeCodeSet(String swypeCode) {
        setSwype(swypeCode);
    }
}
