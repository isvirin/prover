package io.prover.provermvp.camera2;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static io.prover.provermvp.Const.TAG;

/**
 * Created by babay on 08.11.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2Util {
    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param mediaRecorderSizes The list of available decoder sizes
     * @param imageReaderSizes   The list of available imageReader sizes
     * @return The video size
     */
    public static Size chooseVideoSize(Size[] mediaRecorderSizes, Size[] imageReaderSizes, Size desiredAspect) {
        Arrays.sort(mediaRecorderSizes, new CompareSizesByArea());
        for (Size size : mediaRecorderSizes) {
            if (isContainedIn(size, imageReaderSizes) && size.getHeight() <= 1080)
                return size;
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return mediaRecorderSizes[mediaRecorderSizes.length - 1];
    }

    private static boolean isContainedIn(Size size, Size[] sizes) {
        for (Size size1 : sizes) {
            if (size.equals(size1))
                return true;
        }
        return false;
    }

    private static float aspect(Size size) {
        return size.getWidth() / (float) size.getHeight();
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    public static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) rhs.getWidth() * rhs.getHeight() - (long) lhs.getWidth() * lhs.getHeight());
        }

    }
}
