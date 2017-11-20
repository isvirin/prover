package io.prover.provermvp.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by babay on 15.11.2017.
 */

public class ResolutionSelector {
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;

    public Size selectResolution(Size selectedResolution, List<Size> availableResolutions, Size surfaceSize, Context context) {
        Size result;

        if (selectedResolution != null && checkResolutionAvailable(selectedResolution, availableResolutions)) {
            result = selectedResolution;
        } else {
            result = getOptimalVideoSize(surfaceSize, availableResolutions, context.getResources().getDisplayMetrics());
        }

        if (result.getOrientation() != surfaceSize.getOrientation())
            result = result.flip();

        Display display = ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                return result.flip();

            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                return result;
        }
        return result;
    }

    private boolean checkResolutionAvailable(Size selectedResolution, List<Size> availableResolutions) {
        if (selectedResolution != null) {
            for (Size size : availableResolutions) {
                if (selectedResolution.equalsIgnoringRotation(size)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Size getOptimalVideoSize(Size targetSize, List<Size> availableResolutions, DisplayMetrics displayMetrics) {

        int w = targetSize.largerDimension();
        int h = targetSize.smallerDimension();

        final float targetRatio = w / (float) h;

        List<Size> sizesList = new ArrayList<>(availableResolutions);
        Collections.sort(sizesList, new Size.CameraSizeRatioComparator(targetRatio));

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

    public List<Size> getSuitableResolutions(Camera camera, Orientation orientation) {
        Camera.Parameters params = camera.getParameters();
        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        List<Camera.Size> videoSizes = params.getSupportedVideoSizes();
        List<Size> result = new ArrayList<>();

        for (Camera.Size size : previewSizes) {
            int sWidth = size.width;
            int sHeight = size.height;
            if (sWidth >= sHeight && sWidth > MAX_WIDTH || sHeight > MAX_HEIGHT)
                continue;
            if (sWidth < sHeight && sWidth > MAX_HEIGHT || sHeight > MAX_WIDTH)
                continue;

            if (!videoSizes.contains(size))
                continue;
            if (orientation == null)
                result.add(new Size(size));
            else result.add(new Size(size).toOrientation(orientation));
        }

        Collections.sort(result, new Size.CameraAreaComparator());
        return result;
    }

}
