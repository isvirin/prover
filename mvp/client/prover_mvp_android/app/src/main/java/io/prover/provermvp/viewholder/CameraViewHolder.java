package io.prover.provermvp.viewholder;

import android.app.Activity;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.view.Surface;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.File;

import io.prover.provermvp.camera.CameraUtil;
import io.prover.provermvp.camera.MyCamera;
import io.prover.provermvp.camera.ScreenOrientationLock;
import io.prover.provermvp.camera.Size;
import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.permissions.PermissionManager;

import static io.prover.provermvp.camera.CameraUtil.MEDIA_TYPE_VIDEO;

/**
 * Created by babay on 07.11.2017.
 */

public class CameraViewHolder implements ICameraViewHolder {

    private final Activity activity;
    private final ScreenOrientationLock screenOrientationLock = new ScreenOrientationLock();
    private final ViewGroup mRoot;
    private final CameraPreviewHolder previewHolder;
    private final CameraController cameraController;
    private MediaRecorder mMediaRecorder;
    private File videoFile;


    public CameraViewHolder(Activity activity, FrameLayout root, CameraController cameraController) {
        this.activity = activity;
        this.mRoot = root;
        this.cameraController = cameraController;
        MyCamera camera = MyCamera.openBackCamera(cameraController);
        previewHolder = new CameraPreviewHolder(root, camera, cameraController);
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
    public boolean startRecording(Activity activity, float averageFps) {
        if (prepareRecording()) {
            screenOrientationLock.lockScreenOrientation(activity);
            mMediaRecorder.start();
            mRoot.setKeepScreenOn(true);
            cameraController.onRecordingStart(averageFps);
            return true;
        }
        return false;
    }

    @Override
    public void finishRecording() {
        mRoot.setKeepScreenOn(false);
        screenOrientationLock.unlockScreen(activity);
        boolean stoppedOk = false;
        try {
            mMediaRecorder.stop();  // stop the recording
            stoppedOk = true;
        } catch (Exception e) {
        }
        releaseMediaRecorder(); // release the MediaRecorder object
        cameraController.onRecordingStop(stoppedOk ? videoFile : null);

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
        screenOrientationLock.unlockScreen(activity);
        mRoot.setKeepScreenOn(false);
        try {
            mMediaRecorder.stop();  // stop the recording
        } catch (Exception ignored) {
        }
        releaseMediaRecorder(); // release the MediaRecorder object
        videoFile.delete();
        videoFile = null;
        cameraController.onRecordingStop(null);
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
    public void onPause(Activity activity) {
        if (mMediaRecorder != null) {
            cancelRecording();
        }
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
    public void setCameraResolution(Size size) {
        previewHolder.setResolution(size);
    }
}
