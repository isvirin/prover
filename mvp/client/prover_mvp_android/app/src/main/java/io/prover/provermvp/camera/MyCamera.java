package io.prover.provermvp.camera;

import android.content.ContentValues;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.prover.provermvp.Const.TAG;

/**
 * Created by babay on 09.12.2016.
 */

public class MyCamera {
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;
    public final int id;
    private final Camera.CameraInfo cameraInfo;
    private Camera camera;

    private MyCamera(int id, Camera camera, Camera.CameraInfo cameraInfo) {
        this.id = id;
        this.camera = camera;
        this.cameraInfo = cameraInfo;
    }

    public static MyCamera openBackCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    Camera camera = Camera.open(i);
                    return new MyCamera(i, camera, cameraInfo);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
        return null;
    }

    public static String getSceneMode(int width, int height) {
        if (width < height)
            return Camera.Parameters.SCENE_MODE_PORTRAIT;
        else
            return Camera.Parameters.SCENE_MODE_LANDSCAPE;
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

    public Camera open() {
        if (camera != null)
            return camera;
        try {
            camera = Camera.open(id);
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
            camera.unlock();
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

    public Size getOptimalPreviewSize(Camera.Parameters parameters, int width, int height, DisplayMetrics displayMetrics) {
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        int w = width > height ? width : height;
        int h = width > height ? height : width;

        final float targetRatio = w / (float) h;

        List<Size> sizesList = new ArrayList<>(sizes.size());
        for (Camera.Size size : sizes) {
            sizesList.add(new Size(size.width, size.height));
        }

        Collections.sort(sizesList, (o1, o2) -> {
            float ratio1 = Math.abs(o1.ratio - targetRatio);
            float ratio2 = Math.abs(o2.ratio - targetRatio);
            return Float.compare(ratio1, ratio2);
        });

        int minHeight = (int) (h / Math.max(1.5f, displayMetrics.density));
        int maxHeight = (int) (h * 1.25f);

        final float TOLERANCE = 1.2f;
        float bestRatioDiff = Math.abs(sizesList.get(0).ratio - targetRatio);
        if (bestRatioDiff == 0)
            bestRatioDiff = 0.2f;

        for (Size size : sizesList) {
            if (size.height >= minHeight && size.height <= maxHeight) {
                return size;
            }
            if (Math.abs(size.ratio - targetRatio) / bestRatioDiff > TOLERANCE)
                break;
        }

        return sizesList.get(0);
    }

    public Size getOptimalVideoSize(Camera.Parameters parameters, int width, int height, DisplayMetrics displayMetrics) {
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        int w = width > height ? width : height;
        int h = width > height ? height : width;

        final float targetRatio = w / (float) h;

        List<Size> sizesList = new ArrayList<>(sizes.size());
        for (Camera.Size size : sizes) {
            int sWidth = size.width;
            int sHeight = size.height;
            if (sWidth >= sHeight && sWidth > MAX_WIDTH && sHeight > MAX_HEIGHT)
                continue;
            if (sWidth < sHeight && sWidth > MAX_HEIGHT && sHeight > MAX_WIDTH)
                continue;
            sizesList.add(new Size(size.width, size.height));
        }

        Collections.sort(sizesList, (o1, o2) -> {
            float ratio1 = Math.abs(o1.ratio - targetRatio);
            float ratio2 = Math.abs(o2.ratio - targetRatio);
            if (Math.abs(ratio1 - ratio2) < 0.1f)
                return Integer.compare(o2.width * o2.height, o1.width * o1.height);
            return Float.compare(ratio1, ratio2);
        });

        int minHeight = (int) (h / Math.max(1.5f, displayMetrics.density));
        int maxHeight = (int) (h * 1.25f);

        final float TOLERANCE = 1.2f;
        float bestRatioDiff = Math.abs(sizesList.get(0).ratio - targetRatio);
        if (bestRatioDiff == 0)
            bestRatioDiff = 0.2f;

        for (Size size : sizesList) {
            if (size.height >= minHeight && size.height <= maxHeight) {
                return size;
            }
            if (Math.abs(size.ratio - targetRatio) / bestRatioDiff > TOLERANCE)
                break;
        }

        return sizesList.get(0);
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

    public List<Camera.Size> getAvailableSizes() {
        if (camera == null)
            return new ArrayList<>(1);

        return camera.getParameters().getSupportedPreviewSizes();
    }

    public static class Size {
        public final int width;
        public final int height;
        public final float ratio;

        public Size(int w, int h) {
            width = w;
            height = h;
            ratio = width / (float) height;
        }

        public Size(Camera.Size size) {
            this(size.width, size.height);
        }
    }
}
