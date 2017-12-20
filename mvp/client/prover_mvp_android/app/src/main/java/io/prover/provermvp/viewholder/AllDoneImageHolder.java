package io.prover.provermvp.viewholder;

import android.os.Build;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import io.prover.provermvp.R;

/**
 * Created by babay on 10.12.2017.
 */

public class AllDoneImageHolder extends ImageViewHolder {

    VectorDrawableCompat dr;

    public AllDoneImageHolder(ImageView view) {
        super(view);
    }

    public void show() {
        view.setVisibility(View.VISIBLE);
    }

    public void setVectorDrawable() {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        lp.topToBottom = R.id.hintText;
        lp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.topToTop = ConstraintLayout.LayoutParams.UNSET;
        lp.topMargin = (int) (view.getResources().getDisplayMetrics().density * 32);
        lp.rightMargin = 0;
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(lp);
        setVectorDrawable(R.drawable.ic_all_done);
    }

    public void hide() {
        view.setVisibility(View.GONE);
    }

    public void animateMove() {
        applyAnimatedVectorDrawable(R.drawable.ic_all_done_shrink);
        new Handler().postDelayed(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TransitionManager.beginDelayedTransition((ViewGroup) view.getParent());
            }

            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) view.getLayoutParams();

            lp.topToBottom = ConstraintLayout.LayoutParams.UNSET;
            lp.leftToLeft = ConstraintLayout.LayoutParams.UNSET;
            lp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
            lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;

            lp.rightMargin = lp.topMargin = (int) (view.getResources().getDisplayMetrics().density * 16);
            lp.height = (int) (view.getResources().getDisplayMetrics().density * 81);
            lp.width = (int) (view.getResources().getDisplayMetrics().density * 90);
            view.setLayoutParams(lp);
        }, 800);
    }
}
