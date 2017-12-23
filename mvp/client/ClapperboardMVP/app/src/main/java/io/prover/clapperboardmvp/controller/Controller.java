package io.prover.clapperboardmvp.controller;

import android.content.Context;

import org.ethereum.crypto.ECKey;

import io.prover.clapperboardmvp.transport.NetworkHolder;
import io.prover.clapperboardmvp.util.Etherium;


/**
 * Created by babay on 17.11.2017.
 */

public class Controller extends ControllerBase {

    public final NetworkHolder networkHolder;
    boolean resumed;

    public Controller(Context context) {
        Etherium etherium = Etherium.getInstance(context);
        ECKey key = etherium.getKey();
        networkHolder = new NetworkHolder(key, this);
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
