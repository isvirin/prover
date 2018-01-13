package io.prover.provermvp.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

import io.prover.common.transport.NetworkRequest;
import io.prover.common.transport.RequestSwypeCode1;
import io.prover.common.transport.responce.LowFundsException;
import io.prover.provermvp.BuildConfig;
import io.prover.provermvp.R;
import io.prover.provermvp.Settings;
import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.detector.DetectionState;

import static io.prover.provermvp.detector.DetectionState.State.Confirmed;
import static io.prover.provermvp.detector.DetectionState.State.Waiting;

/**
 * Created by babay on 11.11.2017.
 */

public class SwypeStateHelperHolder implements
        CameraController.NetworkRequestErrorListener,
        CameraController.OnRecordingStartListener,
        CameraController.OnRecordingStopListener,
        CameraController.NetworkRequestStartListener,
        CameraController.OnDetectionStateCahngedListener, CameraController.OnSwypeCodeSetListener {
    private final ViewGroup root;
    private final TextView statsText;
    private final CameraController cameraController;
    DetectionState.State latestState = Waiting;
    private String swype;
    private String actualSwype;
    private String swypeStatus;

    public SwypeStateHelperHolder(ViewGroup root, CameraController cameraController) {
        this.root = root;
        statsText = root.findViewById(R.id.statsView);
        this.cameraController = cameraController;
        statsText.bringToFront();
        cameraController.setSwypeStateHelperHolder(this);

        cameraController.onNetworkRequestStart.add(this);
        cameraController.onNetworkRequestError.add(this);
        cameraController.onRecordingStart.add(this);
        cameraController.onRecordingStop.add(this);
        cameraController.detectionState.add(this);
        cameraController.swypeCodeSet.add(this);
        statsText.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDetectionStateChanged(@Nullable DetectionState oldState, @NonNull DetectionState state) {
        float dx = state.x / 1024f;
        float dy = state.y / 1024f;
        String stateStr = String.format(Locale.getDefault(), "%d, %d, %+.3f, %+.3f, %d", state.state.ordinal(), state.index, dx, dy, state.d);
        setStatusText(stateStr);
        latestState = state.state;
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
            if (actualSwype != null)
                builder.append("/").append(actualSwype);
            statsText.setText(builder);
        }
    }

    public void setSwype(String swype, String actualSwypeCode) {
        this.swype = swype;
        this.actualSwype = actualSwypeCode;
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
        return latestState == Confirmed;
    }

    @Override
    public void onRecordingStart() {
        setSwypeStatus("not requesting");
    }

    @Override
    public void onRecordingStop(File file, boolean isVideoConfirmed) {
        setSwype(null, null);
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
    public void onSwypeCodeSet(String swypeCode, String actualSwypeCode) {
        setSwype(swypeCode, actualSwypeCode);
    }
}
