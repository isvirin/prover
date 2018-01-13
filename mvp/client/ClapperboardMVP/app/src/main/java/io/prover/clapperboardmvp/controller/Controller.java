package io.prover.clapperboardmvp.controller;

import android.content.Context;

import io.prover.clapperboardmvp.transport.NetworkHolder;


/**
 * Created by babay on 17.11.2017.
 */

public class Controller extends ControllerBase {

    public final NetworkHolder networkHolder;
    boolean resumed;

    public Controller(Context context) {
        networkHolder = new NetworkHolder(context, this);
    }

    public void onResume() {
        resumed = true;
        networkHolder.doHello();
        handler.postDelayed(() -> {
            if (resumed) networkHolder.doHello();
        }, 30_000);
    }

    public void onPause() {
        resumed = false;
    }

}
