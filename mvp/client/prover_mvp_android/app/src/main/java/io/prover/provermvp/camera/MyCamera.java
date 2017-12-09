package io.prover.provermvp.camera;

import android.content.ContentValues;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.prover.provermvp.Const;
import io.prover.provermvp.Settings;
import io.prover.provermvp.controller.CameraController;

/**
 * Created by babay on 09.12.2016.
 */

public class MyCamera implements CameraController.OnPreviewStartListener, CameraController.OnRecordingStartListener, Camera.PreviewCallback, CameraController.OnFrameReleasedListener, CameraController.OnRecordingStopListener {
    public static final String TAG = Const.TAG + "Camera";
    public final int id;
    private final Camera.CameraInfo cameraInfo;
    private final ResolutionSelector resolutionSelector = new ResolutionSelector();
    private final CameraController cameraController;
    private Camera camera;
    private List<Size> availableResolutions;

    private MyCamera(int id, Camera.CameraInfo cameraInfo, CameraController cameraController) {
        this.id = id;
        this.cameraInfo = cameraInfo;
        this.cameraController = cameraController;
        cameraController.previewStart.add(this);
        cameraController.onRecordingStart.add(this);
        cameraController.onRecordingStop.add(this);
        cameraController.frameReleased.add(this);
        open();
    }

    public static MyCamera openBackCamera(CameraController cameraController) {
        Log.d(TAG, "Open back camera");
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return new MyCamera(i, cameraInfo, cameraController);
            }
        }
        return null;
    }

    public static int getDisplayRotation(int rotationDirection) {
        switch (rotationDirection) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    public synchronized Camera open() {
        if (camera != null)
            return camera;
        try {
            Log.d(TAG, "Camera.open");
            camera = Camera.open(id);
            availableResolutions = resolutionSelector.getSuitableResolutions(camera, null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            camera = null;
        }
        return camera;
    }

    public Camera getCamera() {
        return camera;
    }

    public void release() {
        if (camera != null) {
            Log.d(TAG, "releasing camera");
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    public int getDisplayOrientation(int degrees) {
        // See android.hardware.Camera.setDisplayOrientation for
        // documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(id, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public Size selectResolution(Size selectedResolution, Size surfaceSize, Context context) {
        open();
        if (availableResolutions == null)
            return null;
        return resolutionSelector.selectResolution(selectedResolution, availableResolutions, surfaceSize, context);
    }

    public List<Size> getAvailableResolutions() {
        return availableResolutions;
    }

    public void updateDisplayOrientation(int displayOrientation) {
        int degrees = getDisplayRotation(displayOrientation);
        int orientation = getDisplayOrientation(degrees);
        camera.setDisplayOrientation(orientation);
    }

    public MediaRecorder prepareRecording(File file, Surface previewSurface) {
        if (camera == null)
            return null;

        MediaRecorder mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(camera);

        // Step 2: Set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mediaRecorder.setOutputFile(file.getPath());

        // Step 5: Set the preview output
        mediaRecorder.setPreviewDisplay(previewSurface);

        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            Log.d(ContentValues.TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            try {
                mediaRecorder.reset();
            } catch (Exception ignored) {
            }
            try {
                mediaRecorder.release();
            } catch (Exception ignored) {
            }
            return null;
        }

        return mediaRecorder;
    }

    public void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallbackWithBuffer(null);
            camera.setPreviewCallback(null);
            try {
                camera.setPreviewDisplay(null);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    public void updateCallback() {
        if (camera != null) {
            Log.d(TAG, "updating buffer callback");
            if (Settings.REUSE_PREVIEW_BUFFERS) {
                camera.setPreviewCallbackWithBuffer(this);
            } else {
                camera.setPreviewCallback(this);
            }
        }
    }

    public void setRecording(boolean recording) {
        if (recording) {
            updateCallback();
        }
    }

    @Override
    public void onPreviewStart(@NonNull List<Size> sizes, @NonNull Size previewSize) {
        if (Settings.REUSE_PREVIEW_BUFFERS) {
            int size = previewSize.width * previewSize.height * 3 / 2;
            camera.addCallbackBuffer(new byte[size]);
            camera.addCallbackBuffer(new byte[size]);
            camera.addCallbackBuffer(new byte[size]);
            camera.addCallbackBuffer(new byte[size]);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        cameraController.onFrameAvailable(data, camera);
    }

    @Override
    public void onFrameReleased(byte[] data) {
        if (camera != null) {
            if (Settings.REUSE_PREVIEW_BUFFERS) {
                camera.addCallbackBuffer(data);
            }
        }
    }

    @Override
    public void onRecordingStart(float fps, Size detectorSize) {
        setRecording(true);
    }


    @Override
    public void onRecordingStop(File file, boolean isVideoConfirmed) {
        setRecording(false);
    }
}
