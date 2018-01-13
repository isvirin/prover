package io.prover.common.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by babay on 01.09.2017.
 */

public class SimplePermissionExplainer implements IPermissionExplainer {
    private final int requestTitle;
    private final int requestMessage;
    DialogInterface.OnClickListener positiveListener;

    public SimplePermissionExplainer(int requestTitle, int requestMessage) {
        this.requestTitle = requestTitle;
        this.requestMessage = requestMessage;
    }

    @Override
    public DialogInterface show(Activity activity, int dialogStyle) {
        return new AlertDialog.Builder(activity, dialogStyle)
                .setTitle(requestTitle)
                .setMessage(requestMessage)
                .setPositiveButton(android.R.string.ok, positiveListener)
                .show();
    }

    @Override
    public IPermissionExplainer setPositiveListener(DialogInterface.OnClickListener positiveListener) {
        this.positiveListener = positiveListener;
        return this;
    }

    @Override
    public String getExplanationAfterReject(Context context) {
        return context.getString(requestMessage);
    }
}
