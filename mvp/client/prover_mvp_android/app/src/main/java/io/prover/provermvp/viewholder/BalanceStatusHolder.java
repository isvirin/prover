package io.prover.provermvp.viewholder;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import io.prover.provermvp.R;
import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.transport.HelloRequest;
import io.prover.provermvp.transport.NetworkRequest;
import io.prover.provermvp.transport.RequestSwypeCode1;
import io.prover.provermvp.transport.RequestSwypeCode2;
import io.prover.provermvp.transport.responce.HelloResponce;

/**
 * Created by babay on 22.11.2017.
 */

public class BalanceStatusHolder implements CameraController.NetworkRequestDoneListener, CameraController.NetworkRequestStartListener, CameraController.NetworkRequestErrorListener, View.OnClickListener {
    private final ViewGroup root;
    private final CameraController cameraController;
    private final TextView balanceView;
    private final ImageView proverWalletStatusIcon;
    private VectorDrawableCompat okDrawable;
    private AnimatedVectorDrawable progressDrawable21;

    public BalanceStatusHolder(ViewGroup root, CameraController cameraController) {
        this.root = root.findViewById(R.id.balanceContainer);
        this.cameraController = cameraController;
        balanceView = root.findViewById(R.id.balanceView);
        proverWalletStatusIcon = root.findViewById(R.id.proverWalletStatusIcon);

        cameraController.onNetworkRequestDone.add(this);
        cameraController.onNetworkRequestStart.add(this);
        cameraController.onNetworkRequestError.add(this);

        this.root.setOnClickListener(this);
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

    private void setStatusIconOffline() {
        Drawable dr = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_prover_offline, null);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        proverWalletStatusIcon.setImageDrawable(dr);
        balanceView.setText(R.string.offline);
        balanceView.setCompoundDrawables(null, null, null, null);
    }

    @Override
    public void onNetworkRequestDone(NetworkRequest request, Object responce) {
        if (responce instanceof HelloResponce) {
            HelloResponce hello = (HelloResponce) responce;
            String text = String.format(Locale.getDefault(), "%.6f", hello.getDoubleBalance());
            balanceView.setText(text);

            if (balanceView.getCompoundDrawables()[2] == null) {
                Drawable dr = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_prover_cur, null);
                dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
                balanceView.setCompoundDrawables(null, null, dr, null);
            }
            int color = hello.getDoubleBalance() > 0 ? 0xFFFFFFFF : balanceView.getResources().getColor(R.color.colorAccent);
            balanceView.setTextColor(color);
        }
        if (request instanceof RequestSwypeCode1)
            return;

        cameraController.handler.post(() -> {
            if (cameraController.networkHolder.getTotalRequestsCounter() == 0)
                setStatusIconOk();
        });

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
        if (request instanceof HelloRequest) {
            setStatusIconOffline();
            return;
        }

        if (request instanceof RequestSwypeCode2)
            return;

        cameraController.handler.post(() -> {
            if (cameraController.networkHolder.getTotalRequestsCounter() == 0)
                setStatusIconOk();
        });
    }

    @Override
    public void onClick(View v) {
        cameraController.networkHolder.doHello();
    }
}
