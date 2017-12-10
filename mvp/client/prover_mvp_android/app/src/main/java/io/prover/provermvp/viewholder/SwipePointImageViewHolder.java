package io.prover.provermvp.viewholder;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import io.prover.provermvp.R;

/**
 * Created by babay on 10.12.2017.
 */

public class SwipePointImageViewHolder extends ImageViewHolder {
    @NonNull
    State state = State.None;

    public SwipePointImageViewHolder(ImageView view) {
        super(view);
    }

    public void setState(@NonNull State state) {
        if (this.state == state)
            return;
        switch (state) {
            case None:
                setVectorDrawable(R.drawable.ic_swype_empty);
                break;

            case Unvisited:
                applyAnimatedVectorDrawable(R.drawable.swype_path_point_blink);
                break;

            case Visited:
                applyAnimatedVectorDrawable(R.drawable.swype_path_point_fill);
                break;

            case Failed:
                if (this.state == State.Visited)
                    applyAnimatedVectorDrawable(R.drawable.swype_point_fail_filled);
                else
                    applyAnimatedVectorDrawable(R.drawable.swype_point_fail);
                break;
        }
        this.state = state;
    }

    public void setState(@NonNull State state, Drawable dr) {
        this.state = state;
        view.setImageDrawable(dr);
    }

    public enum State {None, Unvisited, Visited, Failed}
}
