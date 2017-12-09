package io.prover.provermvp.util;

import android.media.Image;
import android.os.Build;
import android.support.annotation.RequiresApi;

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

    private static void recycleS(Frame frame) {
        synchronized (items) {
            if (items.size() < MAX_SIZE) {
                items.add(frame);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setImage(Image image, int timestamp) {
        this.image = image;
        this.format = image.getFormat();
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.timeStamp = timestamp;
    }

    public void recycle() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && image != null) {
            image.close();
            image = null;
        }
        recycleS(this);
    }
}
