package io.prover.provermvp.viewholder;

import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import io.prover.provermvp.R;
import io.prover.provermvp.detector.DetectionState;
import io.prover.provermvp.detector.ProverDetector;
import io.prover.provermvp.detector.SwypeDetectorHandler;
import io.prover.provermvp.transport.BufferHolder;

/**
 * Created by babay on 11.11.2017.
 */

public class SwypeStateHelperHolder implements ProverDetector.DetectionListener {
    private final ViewGroup root;
    private final TextView statsText;
    VideoConfirmedListener confirmedListener;
    int latestState = 0;
    private String swype;
    private String swypeStatus;
    private SwypeDetectorHandler detectorHandler;

    public SwypeStateHelperHolder(ViewGroup root) {
        this.root = root;
        statsText = root.findViewById(R.id.statsView);
        statsText.bringToFront();
    }

    @Override
    public void onDetectionStateChanged(@Nullable DetectionState oldState, @NonNull DetectionState state) {
        String stateStr = String.format(Locale.getDefault(), "%d, %d, %d, %d", state.state, state.index, state.x, state.y);
        setStatusText(stateStr);
        if (state.state == 3 && confirmedListener != null)
            confirmedListener.onVideoConfirmed();
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
    }

    public void setConfirmedListener(VideoConfirmedListener confirmedListener) {
        this.confirmedListener = confirmedListener;
    }

    public boolean isVideoConfirmed() {
        return latestState == 3;
    }

    public void startDetector(float avgFps) {
        detectorHandler = SwypeDetectorHandler.newHandler((int) avgFps * 2, swype, this);
    }

    public void stopDetector() {
        detectorHandler.sendQuit();
        detectorHandler = null;
    }

    public void onPreviewFrame(byte[] data, Camera camera, BufferHolder bufferHolder) {
        if (detectorHandler != null) {
            Camera.Parameters params = camera.getParameters();
            Camera.Size size = params.getPreviewSize();
            if (!detectorHandler.sendProcesstFrame(data, size.width, size.height, bufferHolder)) {
                bufferHolder.releaseBuffer(data);
            }
        } else {
            bufferHolder.releaseBuffer(data);
        }
    }

    public interface VideoConfirmedListener {
        void onVideoConfirmed();
    }
}
