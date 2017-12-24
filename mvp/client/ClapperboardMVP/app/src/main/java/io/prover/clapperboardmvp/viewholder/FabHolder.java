package io.prover.clapperboardmvp.viewholder;

import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;

import io.prover.clapperboardmvp.R;

/**
 * Created by babay on 24.12.2017.
 */

public class FabHolder {
    final FloatingActionButton fab;

    public FabHolder(FloatingActionButton fab) {
        this.fab = fab;
    }

    public void animWalletToClose() {
        AnimatedVectorDrawableCompat dr = AnimatedVectorDrawableCompat.create(fab.getContext(), R.drawable.ic_wallet_to_close);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        ;
        fab.setImageDrawable(dr);
        dr.start();
    }

    public void animCloseToWallet() {
        AnimatedVectorDrawableCompat dr = AnimatedVectorDrawableCompat.create(fab.getContext(), R.drawable.ic_close_to_wallet);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        ;
        fab.setImageDrawable(dr);
        dr.start();
    }
}
