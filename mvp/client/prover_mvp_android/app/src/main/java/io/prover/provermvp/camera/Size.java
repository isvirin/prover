package io.prover.provermvp.camera;

import android.hardware.Camera;

import java.util.Locale;

/**
 * Created by babay on 12.11.2017.
 */
public class Size {
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

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%d x %d", width, height);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o instanceof Size)) {

            Size size = (Size) o;

            return Math.max(width, height) == Math.max(size.width, size.height)
                    && Math.min(width, height) == Math.min(size.width, size.height);
        }
        if (o instanceof Camera.Size) {
            Camera.Size size = (Camera.Size) o;
            return Math.max(width, height) == Math.max(size.width, size.height)
                    && Math.min(width, height) == Math.min(size.width, size.height);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = Math.max(width, height);
        result = 31 * result + Math.min(width, height);
        return result;
    }
}
