package io.prover.provermvp.camera2;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Size;

import java.util.Comparator;

/**
 * Created by babay on 08.11.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2Util {
    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return -Long.signum((long) rhs.getWidth() * rhs.getHeight() - (long) lhs.getWidth() * lhs.getHeight());
        }

    }
}
