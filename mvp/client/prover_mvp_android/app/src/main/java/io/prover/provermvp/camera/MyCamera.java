package io.prover.provermvp.camera;

import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.prover.provermvp.Const.TAG;

/**
 * Created by babay on 09.12.2016.
 */

public class MyCamera {
    public final int id;
    public final Camera camera;
    private final Camera.CameraInfo cameraInfo;

    private MyCamera(int id, Camera camera, Camera.CameraInfo cameraInfo) {
        this.id = id;
        this.camera = camera;
        this.cameraInfo = cameraInfo;
    }

    public static MyCamera open() {
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

    public void release() {
        camera.release();
    }

    public Camera.CameraInfo getCameraInfo() {
        Camera.getCameraInfo(id, cameraInfo);
        return cameraInfo;
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

    public Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height, DisplayMetrics displayMetrics) {
        int w = width > height ? width : height;
        int h = width > height ? height : width;

        final float targetRatio = w / (float) h;

        List<Size> sizesList = new ArrayList<>(sizes.size());
        for (Camera.Size size : sizes) {
            sizesList.add(new Size(size.width, size.height));
        }

        Collections.sort(sizesList, (o1, o2) -> {
            float ratio1 = Math.abs(o1.ratio() - targetRatio);
            float ratio2 = Math.abs(o2.ratio() - targetRatio);
            return Float.compare(ratio1, ratio2);
        });

        int minHeight = (int) (h / Math.max(1.5f, displayMetrics.density));
        int maxHeight = (int) (h * 1.25f);

        final float TOLERANCE = 1.2f;
        float bestRatioDiff = Math.abs(sizesList.get(0).ratio() - targetRatio);
        if (bestRatioDiff == 0)
            bestRatioDiff = 0.2f;

        for (Size size : sizesList) {
            if (size.height >= minHeight && size.height <= maxHeight) {
                return size;
            }
            if (Math.abs(size.ratio() - targetRatio) / bestRatioDiff > TOLERANCE)
                break;
        }

        return sizesList.get(0);
    }

    public void updateDisplayOrientation(int displayOrientation) {
        int degrees = getDisplayRotation(displayOrientation);
        int orientation = getDisplayOrientation(degrees);
        camera.setDisplayOrientation(orientation);
    }

    public static class Size {
        public int width;
        public int height;

        public Size(int w, int h) {
            width = w;
            height = h;
        }

        public Size(Camera.Size size) {
            this(size.width, size.height);
        }

        public float ratio() {
            return width / (float) height;
        }
    }
}
