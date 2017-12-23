package io.prover.clapperboardmvp.viewholder;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import io.prover.clapperboardmvp.R;
import io.prover.clapperboardmvp.controller.Controller;
import io.prover.clapperboardmvp.controller.ControllerBase;
import io.prover.clapperboardmvp.dialog.InfoDialog;
import io.prover.clapperboardmvp.transport.NetworkRequest;
import io.prover.clapperboardmvp.transport.RequestQrCodeFromText2;
import io.prover.clapperboardmvp.transport.responce.HashResponce2;
import io.prover.clapperboardmvp.util.ScreenOrientationLock;

/**
 * Created by babay on 21.12.2017.
 */

public class ControlsViewHolder implements View.OnClickListener, ControllerBase.NetworkRequestDoneListener, ControllerBase.NetworkRequestErrorListener {
    final Controller controller;
    final ViewGroup contentRoot;
    final TextInputLayout textInputLayout;
    final Button getQrCodeButton;
    final TextView largeMessageView;
    final FloatingActionButton fab;
    private final BalanceStatusHolder balanceHolder;
    private final Activity activity;
    private final ScreenOrientationLock screenOrientationLock = new ScreenOrientationLock();
    private final QrCodeViewHolder qrHolder;

    public ControlsViewHolder(Activity activity, Controller controller) {
        this.controller = controller;
        this.activity = activity;
        balanceHolder = new BalanceStatusHolder(activity, controller);
        contentRoot = activity.findViewById(R.id.contentRoot);
        textInputLayout = activity.findViewById(R.id.textInput);
        getQrCodeButton = activity.findViewById(R.id.getQrCodeButton);
        largeMessageView = activity.findViewById(R.id.largeMessageView);
        qrHolder = new QrCodeViewHolder(activity.findViewById(R.id.qrCodeContainer));

        getQrCodeButton.setOnClickListener(this);

        controller.onNetworkRequestDone.add(this);
        controller.onNetworkRequestError.add(this);

        fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getQrCodeButton:
                controller.networkHolder.submitMessageForQrCode(textInputLayout.getEditText().getText().toString());
                largeMessageView.setText("Requesting code");
                TransitionManager.beginDelayedTransition(contentRoot);
                textInputLayout.setEnabled(false);
                getQrCodeButton.setEnabled(false);
                largeMessageView.setVisibility(View.VISIBLE);
                fab.setImageResource(R.drawable.ic_close_black_24dp);
                screenOrientationLock.lockScreenOrientation(activity);
                break;

            case R.id.fab:
                if (getQrCodeButton.isEnabled()) {
                    new InfoDialog(contentRoot.getContext()).show();
                } else {
                    controller.networkHolder.cancelAllRequests();
                    controller.networkHolder.doHello();
                    //TransitionManager.beginDelayedTransition(contentRoot);
                    textInputLayout.setEnabled(true);
                    getQrCodeButton.setEnabled(true);
                    largeMessageView.setVisibility(View.GONE);
                    textInputLayout.setVisibility(View.VISIBLE);
                    getQrCodeButton.setVisibility(View.VISIBLE);
                    fab.setImageResource(R.drawable.ic_wallet);

                    qrHolder.hide();
                    screenOrientationLock.unlockScreen(activity);
                }
                break;
        }
    }

    @Override
    public void onNetworkRequestDone(NetworkRequest request, Object responce) {
        if (request instanceof RequestQrCodeFromText2) {
            qrHolder.setCode((HashResponce2) responce, textInputLayout.getEditText().getText().toString());
            qrHolder.show();
        }
    }

    @Override
    public void onNetworkRequestError(NetworkRequest request, Exception e) {

    }
}
