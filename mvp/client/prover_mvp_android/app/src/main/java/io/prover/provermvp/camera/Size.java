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

    public int getHighQualityBitRate() {
        int width = largerDimension();
        int height = smallerDimension();
        if (height >= 1080)
            return width > 1440 ? 7_680_000 : 5_760_000;


        if (height >= 720)
            return width > 960 ? 3_200_000 : 2_560_000;


        if (height >= 576)
            return width > 768 ? 2_240_000 : 1_600_000;


        if (height >= 480)
            return width > 640 ? 1_600_000 : 1_280_000;

        if (height >= 432)
            return 1_150_000;

        if (height >= 360)
            return width > 480 ? 960_000 : 770_000;

        return 640_000;
    }

    /*
240p	424x240	1.0	0.64	576	64
360p	640x360	1.5	0.96	896	64
432p	768x432	1.8	1.15	1088	64
480p	848x480	2.0	1.28	1216	64
480p HQ	848x480	2.5	1.60	1536	64
576p	1024x576	3.0	1.92	1856	64
576p HQ	1024x576	3.5	2.24	2176	64
720p	1280x720	4.0	2.56	2496	64
720p HQ	1280x720	5.0	3.20	3072	128
1080p	1920x1080	8.0	5.12	4992	128
1080p HQ	1920x1080	12.0	7.68	7552	128
1080p Superbit	1920x1080	N/A	20.32	20000	320

240p	320x240	1.0	0.64	576	64
360p	480x360	1.2	0.77	704	64
480p	640x480	1.5	0.96	896	64
480p HQ	640x480	2.0	1.28	1216	64
576p	768x576	2.3	1.47	1408	64
576p HQ	768x576	2.5	1.60	1536	64
720p	960x720	3.0	1.92	1856	64
720p HQ	960x720	4.0	2.56	2432	128
1080p	1440x1080	6.0	3.84	3712	128
1080p HQ	1440x1080	9.0	5.76	5632	128
1080p Superbit	1440x1080	N/A	20.32	20000	320
     */

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
