package io.prover.provermvp.viewholder;

import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import io.prover.provermvp.R;

/**
 * Created by babay on 15.12.2017.
 */

public class RedPointHolder extends ImageViewHolder {

    private final Matrix pointMatrix = new Matrix();
    private final float[] point = new float[2];
    private boolean visible;

    public RedPointHolder(ImageView view) {
        super(view);
        view.setVisibility(View.GONE);
    }

    public void setVisible(boolean visible) {
        if (this.visible == visible)
            return;

        this.visible = visible;
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            view.bringToFront();
            applyAnimatedVectorDrawable(R.drawable.swype_track_point_blink);
        }
    }

    public void setTranslation(int x, int y) {
        point[0] = x;
        point[1] = y;
        pointMatrix.mapPoints(point);
        view.setTranslationX(point[0]);
        view.setTranslationY(point[1]);
    }

    public void setRedPointPositionMatrixTo(View v, Matrix rotateScaleMatrix) {
        pointMatrix.set(rotateScaleMatrix);
        int dx = view.getWidth() / 2;
        int dy = view.getHeight() / 2;
        if (dx == 0 || dy == 0) {
            Drawable dr = view.getDrawable();
            dx = dr.getIntrinsicWidth() / 2;
            dy = dr.getIntrinsicHeight() / 2;
        }
        pointMatrix.postTranslate(-dx, -dy);
        pointMatrix.postTranslate((v.getLeft() + v.getRight()) / 2, (v.getTop() + v.getBottom()) / 2);
    }
}
