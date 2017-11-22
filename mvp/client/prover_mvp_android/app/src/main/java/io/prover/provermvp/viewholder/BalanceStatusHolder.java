package io.prover.provermvp.viewholder;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import io.prover.provermvp.R;
import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.transport.NetworkRequest;
import io.prover.provermvp.transport.RequestSwypeCode1;
import io.prover.provermvp.transport.RequestSwypeCode2;
import io.prover.provermvp.transport.responce.HelloResponce;

/**
 * Created by babay on 22.11.2017.
 */

public class BalanceStatusHolder implements CameraController.NetworkRequestDoneListener, CameraController.NetworkRequestStartListener, CameraController.NetworkRequestErrorListener {
    private final ViewGroup root;
    private final CameraController cameraController;
    private final TextView balanceView;
    private final ImageView proverWalletStatusIcon;
    private VectorDrawableCompat okDrawable;
    private AnimatedVectorDrawable progressDrawable21;

    public BalanceStatusHolder(ViewGroup root, CameraController cameraController) {
        this.root = root;
        this.cameraController = cameraController;
        balanceView = root.findViewById(R.id.balanceView);
        proverWalletStatusIcon = root.findViewById(R.id.proverWalletStatusIcon);

        cameraController.onNetworkRequestDone.add(this);
        cameraController.onNetworkRequestStart.add(this);
        cameraController.onNetworkRequestError.add(this);
    }

    private void setStatusIconOk() {
        if (okDrawable == null) {
            okDrawable = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_prover_ok, null);
            okDrawable.setBounds(0, 0, okDrawable.getIntrinsicWidth(), okDrawable.getIntrinsicHeight());
        }
        proverWalletStatusIcon.setImageDrawable(okDrawable);
        if (progressDrawable21 != null)
            progressDrawable21.stop();
    }

    @Override
    public void onNetworkRequestDone(NetworkRequest request, Object responce) {
        if (responce instanceof HelloResponce) {
            String text = String.format(Locale.getDefault(), "%.4f", ((HelloResponce) responce).getDoubleBalance());
            balanceView.setText(text);
        }
        if (request instanceof RequestSwypeCode1)
            return;

        setStatusIconOk();
    }

    @Override
    public void onNetworkRequestStart(NetworkRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (progressDrawable21 == null) {
                progressDrawable21 = (AnimatedVectorDrawable) root.getResources().getDrawable(R.drawable.ic_prover_connecting_animated);
                progressDrawable21.setBounds(0, 0, progressDrawable21.getIntrinsicWidth(), progressDrawable21.getIntrinsicHeight());
            }
            proverWalletStatusIcon.setImageDrawable(progressDrawable21);
            progressDrawable21.start();
        }
    }

    @Override
    public void onNetworkRequestError(NetworkRequest request, Exception e) {
        if (request instanceof RequestSwypeCode2)
            return;

        setStatusIconOk();
    }
}
