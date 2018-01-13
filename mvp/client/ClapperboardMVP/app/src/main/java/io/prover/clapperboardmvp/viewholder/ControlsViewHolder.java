package io.prover.clapperboardmvp.viewholder;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import io.prover.clapperboardmvp.MainActivity;
import io.prover.clapperboardmvp.R;
import io.prover.clapperboardmvp.controller.Controller;
import io.prover.clapperboardmvp.controller.ControllerBase;
import io.prover.common.dialog.InfoDialog;
import io.prover.common.transport.NetworkRequest;
import io.prover.common.transport.RequestQrCodeFromText2;
import io.prover.common.transport.responce.HashResponce2;
import io.prover.common.transport.responce.TemporaryDenyException;
import io.prover.common.util.ScreenOrientationLock;

/**
 * Created by babay on 21.12.2017.
 */

public class ControlsViewHolder implements View.OnClickListener, ControllerBase.NetworkRequestDoneListener, ControllerBase.NetworkRequestErrorListener {
    final Controller controller;
    final ViewGroup contentRoot;
    final TextInputLayout textInputLayout;
    final Button getQrCodeButton;
    final TextView largeMessageView;
    final FabHolder fabHolder;
    private final BalanceStatusHolder balanceHolder;
    private final MainActivity activity;
    private final ScreenOrientationLock screenOrientationLock = new ScreenOrientationLock();
    private final QrCodeViewHolder qrHolder;

    private Mode mode = Mode.Initial;

    public ControlsViewHolder(MainActivity activity, Controller controller) {
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

        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(this);
        fabHolder = new FabHolder(fab);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getQrCodeButton:
                controller.networkHolder.submitMessageForQrCode(textInputLayout.getEditText().getText().toString());
                setMode(Mode.ExpectingQrCode);
                break;

            case R.id.fab:
                if (getQrCodeButton.isEnabled()) {
                    new InfoDialog(activity, activity).show();
                } else {
                    controller.networkHolder.cancelAllRequests();
                    controller.networkHolder.doHello();
                    setMode(Mode.Initial);
                }
                break;
        }
    }

    private void setMode(Mode mode) {
        this.mode = mode;
        switch (mode) {
            case Initial:
                textInputLayout.setEnabled(true);
                getQrCodeButton.setEnabled(true);
                largeMessageView.setVisibility(View.GONE);
                textInputLayout.setVisibility(View.VISIBLE);
                getQrCodeButton.setVisibility(View.VISIBLE);
                fabHolder.animCloseToWallet();
                qrHolder.hide();
                screenOrientationLock.unlockScreen(activity);
                break;

            case ExpectingQrCode:
                largeMessageView.setText(R.string.requestingQrCode);
                TransitionManager.beginDelayedTransition(contentRoot);
                textInputLayout.setEnabled(false);
                getQrCodeButton.setEnabled(false);
                largeMessageView.setVisibility(View.VISIBLE);
                fabHolder.animWalletToClose();
                screenOrientationLock.lockScreenOrientation(activity);
                break;

            case ShowingQrCode:
                qrHolder.show();
                break;
        }
    }

    @Override
    public void onNetworkRequestDone(NetworkRequest request, Object responce) {
        if (request instanceof RequestQrCodeFromText2) {
            qrHolder.setCode((HashResponce2) responce, textInputLayout.getEditText().getText().toString());
            setMode(Mode.ShowingQrCode);
            textInputLayout.getEditText().setText("");
        }
    }

    @Override
    public void onNetworkRequestError(NetworkRequest request, Exception e) {
        if (!(e instanceof TemporaryDenyException) && mode == Mode.ExpectingQrCode) {
            controller.handler.postDelayed(() -> setMode(Mode.Initial), 400);
        }
    }

    private enum Mode {Initial, ExpectingQrCode, ShowingQrCode}
}
