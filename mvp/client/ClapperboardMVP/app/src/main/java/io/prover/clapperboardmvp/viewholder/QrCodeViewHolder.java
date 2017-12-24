package io.prover.clapperboardmvp.viewholder;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.transition.ChangeBounds;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.spongycastle.util.BigIntegers;

import io.nayuki.qrcodegen.QrCode;
import io.prover.clapperboardmvp.BuildConfig;
import io.prover.clapperboardmvp.R;
import io.prover.clapperboardmvp.transport.responce.HashResponce2;

import static io.prover.clapperboardmvp.Const.TAG;

/**
 * Created by babay on 23.12.2017.
 */

public class QrCodeViewHolder {
    final ImageView qrCodeImage;
    final View generatedBy;
    final View proverLogo;
    final TextView originalText;
    ConstraintLayout root;

    public QrCodeViewHolder(ConstraintLayout root) {
        this.root = root;
        qrCodeImage = root.findViewById(R.id.qrCodeImage);
        generatedBy = root.findViewById(R.id.generatedBy);
        proverLogo = root.findViewById(R.id.proverLogo);
        originalText = root.findViewById(R.id.originalText);
    }

    public void setCode(HashResponce2 responce2, String message) {
        originalText.setText(message);

        byte[] values = new byte[46];
        System.arraycopy(responce2.hashResponce1.hashBytes, 0, values, 0, 32);
        System.arraycopy(responce2.hashBytes, 0, values, 32, values.length - 32);

        String digits = BigIntegers.fromUnsignedByteArray(values).toString(10);
        QrCode code = QrCode.encodeNumeric(digits, QrCode.Ecc.QUARTILE);

        DisplayMetrics dm = root.getResources().getDisplayMetrics();
        int scrSize = Math.min(dm.widthPixels, dm.heightPixels);
        int pad = (int) (dm.density * 16);
        int border = 0;
        int scale = (scrSize - 2 * pad) / (code.size + border * 2);

        VectorDrawableCompat dr = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_prover_qrcode, null);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        VectorDrawableCompat dr2 = VectorDrawableCompat.create(root.getResources(), R.drawable.ic_prover_qrcode_light, null);
        dr2.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        Bitmap bitmap = code.toImage(scale, border, Bitmap.Config.ARGB_8888, dr, dr2);
        qrCodeImage.setImageBitmap(bitmap);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("set qr-code transaction: %s, block: %s", responce2.hashResponce1.hashString, responce2.hashString));
        }
    }

    public void show() {
        root.setVisibility(View.INVISIBLE);
        new Handler().post(() -> {
            int w = root.getWidth() / 2;
            int h = root.getHeight() / 2;
            root.setClipBounds(new Rect(w, h, w, h));
            root.setVisibility(View.VISIBLE);
            TransitionManager.beginDelayedTransition((ViewGroup) root.getParent(), changeRootBoundsTransaction());
            root.setClipBounds(null);
        });
    }

    public void hide() {
        TransitionManager.beginDelayedTransition((ViewGroup) root.getParent(), changeRootBoundsTransaction());
        int w = root.getWidth() / 2;
        int h = root.getHeight() / 2;
        root.setClipBounds(new Rect(w, h, w, h));
    }

    private Transition changeRootBoundsTransaction() {
        ChangeBounds tr = new ChangeBounds();
        tr.addTarget(root).setDuration(400);
        tr.setResizeClip(true);
        return tr;
    }
}
