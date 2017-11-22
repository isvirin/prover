package io.prover.provermvp.dialog;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.ethereum.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

import io.prover.provermvp.R;
import io.prover.provermvp.util.Etherium;

/**
 * Created by babay on 15.11.2017.
 */

public class InfoDialog extends Dialog implements View.OnClickListener {

    private final TextView addressView;
    private String address;

    public InfoDialog(@NonNull Context context) {
        super(context);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_info);
        addressView = findViewById(R.id.addressView);

        updateAddress();
        addressView.setOnClickListener(this);
        findViewById(R.id.buttonOk).setOnClickListener(this);
        setTitle(R.string.info);
    }

    private void updateAddress() {
        Etherium etherium = Etherium.getInstance(getContext());
        ECKey key = etherium.getKey();
        if (key != null) {
            byte[] addressBytes = key.getAddress();
            byte[] addDigitBytes = Hex.encode(addressBytes);
            address = "0x" + new String(addDigitBytes);

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(getContext().getString(R.string.address_)).append("\n");
            int start = builder.length();
            builder.append(address);
            int end = builder.length();
            Object span = new RelativeSizeSpan(1.2f);
            builder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            Resources res = getContext().getResources();
            TypedValue val = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, val, true);
            int color = res.getColor(val.resourceId);

            span = new ForegroundColorSpan(color);
            builder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            addressView.setText(builder);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addressView:
                if (address != null) {
                    Context context = v.getContext();

                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(address, address);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, R.string.addressCopied, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.buttonOk:
                dismiss();
                break;
        }
    }
}
