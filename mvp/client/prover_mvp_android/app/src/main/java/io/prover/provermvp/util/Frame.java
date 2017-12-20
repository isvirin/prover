package io.prover.provermvp.util;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.Image;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by babay on 09.12.2017.
 */

public class Frame {
    private static final int MAX_SIZE = 20;
    private static final List<Frame> items = new ArrayList<>(MAX_SIZE);

    public Image image;
    public int timeStamp;
    public int width;
    public int height;
    public int format;
    public byte[] data;
    private ReleaseListener releaseListener;

    public static Frame obtain(Image image) {
        Frame frame = obtain();
        frame.image = image;
        frame.format = image.getFormat();
        frame.width = image.getWidth();
        frame.height = image.getHeight();
        return frame;
    }

    public static Frame obtain(byte[] data, Camera camera, ReleaseListener releaseListener) {
        Camera.Parameters params = camera.getParameters();
        Camera.Size size = params.getPreviewSize();

        Frame frame = obtain();
        frame.data = data;
        frame.format = ImageFormat.NV21;
        frame.width = size.width;
        frame.height = size.height;
        frame.releaseListener = releaseListener;
        return frame;
    }

    public static Frame obtain() {
        Frame frame = null;
        synchronized (items) {
            if (items.size() > 0) {
                frame = items.remove(items.size() - 1);
            }
        }
        if (frame == null) {
            frame = new Frame();
        }
        return frame;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void recycle() {
        if (releaseListener != null) {
            releaseListener.onFrameRelease(this);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && image != null) {
            image.close();
            image = null;
        } else if (data != null) {
            data = null;
        }

        synchronized (items) {
            if (items.size() < MAX_SIZE) {
                items.add(this);
            }
        }
    }

    public interface ReleaseListener {
        void onFrameRelease(Frame frame);
    }
}
