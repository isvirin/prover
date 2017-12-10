package io.prover.provermvp.viewholder;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.view.View;
import android.widget.ImageView;

import io.prover.provermvp.R;
import io.prover.provermvp.util.SwypeDirection;

/**
 * Created by babay on 23.11.2017.
 */

public class SwypeArrowHolder {
    private final ConstraintLayout root;
    private final SwipePointImageViewHolder[] swypePoints;
    private final ImageView arrowView;

    public SwypeArrowHolder(ConstraintLayout root, SwipePointImageViewHolder[] swypePoints) {
        this.root = root;
        this.swypePoints = swypePoints;
        arrowView = root.findViewById(R.id.swypeArrow);
    }

    public void hide() {
        arrowView.setVisibility(View.GONE);
    }

    public void show(int from, int to) {
        arrowView.setVisibility(View.GONE);
        SwypeDirection direction = SwypeDirection.ofTwoSwypePoints(from, to);
        if (direction == null) {
            return;
        }

        Drawable dr = getDrawable(direction);
        if (dr == null) {
            return;
        }
        int angle = getRotationAngle(direction);
        arrowView.setImageDrawable(dr);

        arrowView.setPivotX(dr.getIntrinsicWidth() / 2);
        arrowView.setPivotY(dr.getIntrinsicHeight() / 2);

        arrowView.setRotation(angle);

        positionAtView(swypePoints[from].view, direction, dr);
        arrowView.setVisibility(View.VISIBLE);
        if (dr instanceof Animatable)
            ((Animatable) dr).start();
    }

    private Drawable getDrawable(SwypeDirection direction) {
        switch (direction) {
            case Up:
            case Down:
            case Left:
            case Right:
                Drawable dr = AnimatedVectorDrawableCompat.create(root.getContext(), R.drawable.ic_arror_right_animated);
                dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
                return dr;

            case UpLeft:
            case UpRight:
            case DownLeft:
            case DownRight:
                dr = AnimatedVectorDrawableCompat.create(root.getContext(), R.drawable.ic_arror_diagonal_animated);
                dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
                return dr;
        }
        return null;
    }

    private int getRotationAngle(SwypeDirection direction) {
        switch (direction) {
            case Up:
            case Down:
            case Left:
            case Right:
                return direction.degreesTo(SwypeDirection.Right);

            case UpLeft:
            case UpRight:
            case DownLeft:
            case DownRight:
                return direction.degreesTo(SwypeDirection.UpRight);
        }
        return 0;
    }

    private void positionAtView(View view, SwypeDirection direction, Drawable dr) {
        int width = dr.getIntrinsicWidth();
        int height = dr.getIntrinsicHeight();
        int cX = (view.getLeft() + view.getRight()) / 2;
        int cY = (view.getTop() + view.getBottom()) / 2;
        int viewSize = view.getWidth();
        if (direction.isHorizontal()) {
            arrowView.setTranslationY(cY - height / 2);
            if (direction == SwypeDirection.Right)
                arrowView.setTranslationX(cX + viewSize / 2);
            else
                arrowView.setTranslationX(cX - viewSize / 2 - width);
        } else if (direction.isVertical()) {
            arrowView.setTranslationX(cX - width / 2);
            int shift = viewSize / 2 + (width - height) / 2;
            if (direction == SwypeDirection.Down)
                arrowView.setTranslationY(cY + shift);
            else
                arrowView.setTranslationY(cY - shift - height);
        } else {
            viewSize = (int) (viewSize / 1.41);
            int x = cX + viewSize / 2 * direction.dx;
            int y = cY + viewSize / 2 * direction.dy;
            if (direction.dx < 0)
                x -= width;
            if (direction.dy < 0)
                y -= height;
            arrowView.setTranslationX(x);
            arrowView.setTranslationY(y);
        }
    }
}