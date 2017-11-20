package io.prover.provermvp.camera;

import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Comparator;
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Size(android.util.Size size) {
        this(size.getWidth(), size.getHeight());
    }

    public static Size fromPreferences(SharedPreferences prefs, String keyX, String keyY) {
        if (prefs.contains(keyX) && prefs.contains(keyY)) {
            int w = prefs.getInt(keyX, 0);
            int h = prefs.getInt(keyY, 0);
            return new Size(w, h);
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%d x %d", width, height);
    }

    public boolean equalsIgnoringRotation(Object o) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o instanceof Size)) {
            Size size = (Size) o;
            return width == size.width && height == size.height;
        }
        if (o instanceof Camera.Size) {
            Camera.Size size = (Camera.Size) o;
            return width == size.width && height == size.height;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = Math.max(width, height);
        result = 31 * result + Math.min(width, height);
        return result;
    }

    public Size toLandscape() {
        return width < height ? new Size(height, width) : this;
    }

    public Size toPortrait() {
        return width > height ? new Size(height, width) : this;
    }

    public Size flip() {
        return new Size(height, width);
    }

    public int largerDimension() {
        return width > height ? width : height;
    }

    public int smallerDimension() {
        return width < height ? width : height;
    }

    public Orientation getOrientation() {
        return width >= height ? Orientation.Landscape : Orientation.Portrait;
    }

    public Size toOrientation(Orientation orientation) {
        return getOrientation() == orientation ? this : flip();
    }

    public int area() {
        return width * height;
    }

    public Size scale(float scaleWidth, float scaleHeight) {
        return new Size((int) (width * scaleWidth), (int) (height * scaleHeight));
    }

    public void saveToPreferences(SharedPreferences.Editor edit, String keyX, String keyY) {
        edit.putInt(keyX, width).putInt(keyY, height);
    }

    public static class CameraAreaComparator implements Comparator<Size> {

        @Override
        public int compare(Size o1, Size o2) {
            return Integer.compare(o2.width * o2.height, o1.width * o1.height);
        }
    }

    public static class CameraSizeRatioComparator implements Comparator<Size> {
        final float targetRatio;

        public CameraSizeRatioComparator(float targetRatio) {
            this.targetRatio = targetRatio;
        }

        @Override
        public int compare(Size o1, Size o2) {
            float ratio1 = Math.abs(o1.ratio - targetRatio);
            float ratio2 = Math.abs(o2.ratio - targetRatio);
            if (Math.abs(ratio1 - ratio2) < 0.1f)
                return Integer.compare(o2.width * o2.height, o1.width * o1.height);
            return Float.compare(ratio1, ratio2);
        }
    }
}
