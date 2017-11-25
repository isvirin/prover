package io.prover.provermvp.camera2;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.spongycastle.util.Arrays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.prover.provermvp.camera.Size;

import static io.prover.provermvp.Const.TAG;

/**
 * Created by babay on 17.11.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2PrefsHelper {
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;

    private static <T> boolean arrayContains(T[] array, T needle) {
        for (T value : array) {
            if (needle.equals(value))
                return true;
        }
        return false;
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param surfaceSize The minimum desired width
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    public Size chooseOptimalSize(android.util.Size[] choices, Size surfaceSize, Size aspectRatio, float maxAspectDeviation) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        Size surfaceSize2 = surfaceSize.toOrientation(aspectRatio.getOrientation());
        List<android.util.Size> bigEnough = new ArrayList<>();
        for (android.util.Size option : choices) {
            if (option.getWidth() >= surfaceSize2.width && option.getHeight() >= surfaceSize2.height) {
                float aspect = option.getWidth() / (float) option.getHeight();
                if (Math.abs(aspect - aspectRatio.ratio) < maxAspectDeviation) {
                    bigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            android.util.Size size = Collections.min(bigEnough, new Camera2Util.CompareSizesByArea());
            return new Size(size);
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return new Size(choices[0]);
        }
    }

    public String selectBackCameraId(@NonNull CameraManager manager) throws CameraAccessException {
        String[] idList = manager.getCameraIdList();
        for (String cameraId : idList) {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing == CameraCharacteristics.LENS_FACING_BACK)
                return cameraId;
        }
        return idList[0];
    }

    public List<Size> loadCameraResolutions(StreamConfigurationMap map, int imageFormat) throws CameraAccessException {
        android.util.Size[] videoSizes = map.getOutputSizes(MediaRecorder.class);

        List<Size> result = new ArrayList<>();

        for (android.util.Size size : videoSizes) {
            int sWidth = size.getWidth();
            int sHeight = size.getHeight();
            if (sWidth >= sHeight && sWidth > MAX_WIDTH || sHeight > MAX_HEIGHT)
                continue;
            if (sWidth < sHeight && sWidth > MAX_HEIGHT || sHeight > MAX_WIDTH)
                continue;

            /*if (!arrayContains(imageSizes, size))
                continue;*/
            result.add(new Size(size));
        }

        Collections.sort(result, new Size.CameraAreaComparator());

        return result;
    }

    public int selectFormat(StreamConfigurationMap map) {
        int[] outFormats = map.getOutputFormats();
        if (Arrays.contains(outFormats, ImageFormat.YUV_420_888)) {
            return ImageFormat.YUV_420_888;
        }
        if (Arrays.contains(outFormats, ImageFormat.NV21))
            return ImageFormat.NV21;
/*        if (Arrays.contains(outFormats, ImageFormat.YV12)){
            return ImageFormat.YV12;
        }*/

        return ImageFormat.YUV_420_888;
    }
}
