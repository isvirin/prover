package io.prover.provermvp.camera2;

import android.util.SparseIntArray;
import android.view.Surface;

/**
 * Created by babay on 19.11.2017.
 */

public class OrientationHelper {
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    public static Integer getOrientationHint(int sensorOrientation, int rotation) {
        switch (sensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                return DEFAULT_ORIENTATIONS.get(rotation);
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                return INVERSE_ORIENTATIONS.get(rotation);
        }
        return null;
    }
}
