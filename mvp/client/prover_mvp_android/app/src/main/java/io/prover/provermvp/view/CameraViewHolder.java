package io.prover.provermvp.view;

import android.app.Activity;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.util.Log;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;

import io.prover.provermvp.camera.CameraPreview;
import io.prover.provermvp.camera.CameraUtil;
import io.prover.provermvp.camera.MyCamera;
import io.prover.provermvp.camera.ScreenOrientationLock;
import io.prover.provermvp.permissions.PermissionManager;

import static android.content.ContentValues.TAG;
import static io.prover.provermvp.camera.CameraUtil.MEDIA_TYPE_VIDEO;

/**
 * Created by babay on 07.11.2017.
 */

public class CameraViewHolder {

    private final ScreenOrientationLock screenOrientationLock = new ScreenOrientationLock();
    private final ViewGroup mRoot;

    private CameraPreview cameraPreview;
    private MediaRecorder mMediaRecorder;
    private File videoFile;
    private boolean resumed;

    public CameraViewHolder(ViewGroup root) {
        this.mRoot = root;
    }

    public void startCamera() {
        if (resumed && CameraUtil.checkCameraHardware(mRoot.getContext())) {
            if (mMediaRecorder != null)
                return;

            if (cameraPreview != null) {
                cameraPreview.releaseCamera();
                mRoot.removeView(cameraPreview);
            }
            MyCamera camera = MyCamera.open();
            if (camera != null) {
                cameraPreview = new CameraPreview(mRoot.getContext(), camera);
                mRoot.addView(cameraPreview);
            }
        }
    }

    private boolean prepareRecording() {
        cameraPreview.getCamera().unlock();
        cameraPreview.recording = true;
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setCamera(cameraPreview.getCamera());

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        videoFile = CameraUtil.getOutputMediaFile(MEDIA_TYPE_VIDEO, mRoot.getContext());
        mMediaRecorder.setOutputFile(videoFile.getPath());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }

        //Toast.makeText(mRoot.getContext(), "save to: " + videoFile.getPath(), Toast.LENGTH_LONG).show();
        return true;
    }

    public boolean startRecording(Activity activity) {
        if (prepareRecording()) {
            screenOrientationLock.lockScreenOrientation(activity);
            mMediaRecorder.start();
            return true;
        }
        return false;
    }

    public void finishRecording() {
        boolean stoppedOk = false;
        try {
            mMediaRecorder.stop();  // stop the recording
            stoppedOk = true;
        } catch (Exception e) {
        }
        releaseMediaRecorder(); // release the MediaRecorder object
        cameraPreview.getCamera().lock();         // take camera access back from MediaRecorder
        cameraPreview.recording = false;
        if (stoppedOk) {
            MediaScannerConnection.scanFile(mRoot.getContext(), new String[]{videoFile.getAbsolutePath()}, null, null);
        } else {
            videoFile.delete();
            videoFile = null;
        }
    }

    public void cancelRecording() {
        mMediaRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        cameraPreview.getCamera().lock();         // take camera access back from MediaRecorder
        cameraPreview.recording = false;
        videoFile.delete();
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            cameraPreview.getCamera().lock();           // lock camera for later use
        }
    }

    public boolean isRecording() {
        return mMediaRecorder != null;
    }

    public File getVideoFile() {
        return videoFile;
    }

    public void onPause(Activity activity) {
        resumed = false;
        if (activity.isChangingConfigurations()) {
            if (cameraPreview != null) {
                cameraPreview.onChangingOrientation();
            }
            return;
        }

        screenOrientationLock.unlockScreen(activity);
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        if (cameraPreview != null) {
            cameraPreview.releaseCamera();              // release the camera immediately on pause event
        }
    }

    public void onResume(Activity activity) {
        resumed = true;
        PermissionManager.ensureHaveCameraPermission(activity, this::startCamera);
    }
}
