package io.prover.provermvp.viewholder;

import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.view.Surface;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Locale;

import io.prover.provermvp.R;
import io.prover.provermvp.camera.CameraUtil;
import io.prover.provermvp.camera.MyCamera;
import io.prover.provermvp.camera.ScreenOrientationLock;
import io.prover.provermvp.camera.Size;
import io.prover.provermvp.permissions.PermissionManager;
import io.prover.provermvp.transport.BufferHolder;
import io.prover.provermvp.util.FrameRateCounter;

import static io.prover.provermvp.camera.CameraUtil.MEDIA_TYPE_VIDEO;

/**
 * Created by babay on 07.11.2017.
 */

public class CameraViewHolder implements ICameraViewHolder, Camera.PreviewCallback {

    private final Activity activity;
    private final ScreenOrientationLock screenOrientationLock = new ScreenOrientationLock();
    private final ViewGroup mRoot;
    private final CameraPreviewHolder previewHolder;
    private final TextView fpsView;
    private final FrameRateCounter fpsCounter = new FrameRateCounter(60, 10);
    private final SwypeStateHelperHolder swypeStateHelperHolder;
    private MediaRecorder mMediaRecorder;
    private File videoFile;


    public CameraViewHolder(Activity activity, FrameLayout root, SwypeStateHelperHolder stateHelperHolder) {
        this.activity = activity;
        this.mRoot = root;
        fpsView = root.findViewById(R.id.fpsCounter);
        this.swypeStateHelperHolder = stateHelperHolder;
        MyCamera camera = MyCamera.openBackCamera();
        previewHolder = new CameraPreviewHolder(root, camera, this);
        if (camera != null)
            camera.setPreviewCallback(this);
        fpsView.bringToFront();
    }

    private boolean prepareRecording() {
        Surface surface = previewHolder.getSurface();
        if (surface == null)
            return false;
        videoFile = CameraUtil.getOutputMediaFile(MEDIA_TYPE_VIDEO, mRoot.getContext());
        previewHolder.unlockCamera();
        mMediaRecorder = previewHolder.getCamera().prepareRecording(videoFile, surface);

        if (mMediaRecorder == null) {
            previewHolder.lockCamera();
            return false;
        }
        return true;
    }

    @Override
    public boolean startRecording(Activity activity) {
        if (prepareRecording()) {
            screenOrientationLock.lockScreenOrientation(activity);
            mMediaRecorder.start();
            mRoot.setKeepScreenOn(true);
            swypeStateHelperHolder.startDetector(fpsCounter.getAvgFps());
            previewHolder.getCamera().setRecording(true);
            return true;
        }
        return false;
    }

    @Override
    public void finishRecording() {
        mRoot.setKeepScreenOn(false);
        previewHolder.getCamera().setRecording(false);
        screenOrientationLock.unlockScreen(activity);
        boolean stoppedOk = false;
        try {
            mMediaRecorder.stop();  // stop the recording
            stoppedOk = true;
        } catch (Exception e) {
        }
        previewHolder.getCamera().setRecording(false);
        releaseMediaRecorder(); // release the MediaRecorder object
        swypeStateHelperHolder.stopDetector();
        if (stoppedOk) {
            MediaScannerConnection.scanFile(mRoot.getContext(), new String[]{videoFile.getAbsolutePath()}, null, null);
        } else {
            videoFile.delete();
            videoFile = null;
        }
        previewHolder.lockCamera();           // lock camera for later use
    }

    @Override
    public void cancelRecording() {
        previewHolder.getCamera().setRecording(false);
        mRoot.setKeepScreenOn(false);
        try {
            mMediaRecorder.stop();  // stop the recording
        } catch (Exception ignored) {
        }
        releaseMediaRecorder(); // release the MediaRecorder object
        swypeStateHelperHolder.stopDetector();
        videoFile.delete();
        previewHolder.lockCamera();           // lock camera for later use
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
        }
    }

    @Override
    public boolean isRecording() {
        return mMediaRecorder != null;
    }

    @Override
    public File getVideoFile() {
        return videoFile;
    }

    @Override
    public void onPause(Activity activity) {
        screenOrientationLock.unlockScreen(activity);
        previewHolder.releaseCamera();
    }

    @Override
    public void onResume(Activity activity) {
        previewHolder.onResume();
        boolean hasPermissions = PermissionManager.ensureHaveCameraPermission(activity, null);
        previewHolder.setHasPermissions(hasPermissions);
    }

    @Override
    public void onStop() {
        previewHolder.releaseCamera();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        float fps = fpsCounter.addFrame();
        BufferHolder bufferHolder = previewHolder.getCamera().getBufferHolder();
        bufferHolder.onBufferGotFromCamera();
        swypeStateHelperHolder.onPreviewFrame(data, camera, bufferHolder);
        if (fps >= 0) {
            fpsView.setText(String.format(Locale.getDefault(), "%.1f", fps));
        }
    }

    @Override
    public List<Size> getCameraResolutions() {
        return previewHolder.getCameraResolutions();
    }

    @Override
    public Size getSelectedCameraResolution() {
        return previewHolder.getSelectedCameraResolution();
    }

    @Override
    public void setCameraResolution(Size size) {
        previewHolder.setResolution(size);
    }
}
