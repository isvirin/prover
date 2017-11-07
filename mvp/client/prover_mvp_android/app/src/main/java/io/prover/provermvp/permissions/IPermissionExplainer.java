package io.prover.provermvp.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by babay on 01.09.2017.
 */

public interface IPermissionExplainer {
    DialogInterface show(Activity activity, int dialogStyle);

    IPermissionExplainer setPositiveListener(DialogInterface.OnClickListener positiveListener);

    String getExplanationAfterReject(Context context);
}
