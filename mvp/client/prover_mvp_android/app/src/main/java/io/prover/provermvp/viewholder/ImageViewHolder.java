package io.prover.provermvp.viewholder;

import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.widget.ImageView;

/**
 * Created by babay on 10.12.2017.
 */

public class ImageViewHolder {
    public final ImageView view;

    public ImageViewHolder(ImageView view) {
        this.view = view;
    }

    public void applyAnimatedVectorDrawable(int id) {
        AnimatedVectorDrawableCompat dr = AnimatedVectorDrawableCompat.create(view.getContext(), id);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        view.setImageDrawable(dr);
        dr.start();
    }

    public void setVectorDrawable(int id) {
        VectorDrawableCompat dr = VectorDrawableCompat.create(view.getResources(), id, null);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        view.setImageDrawable(dr);
    }
}
