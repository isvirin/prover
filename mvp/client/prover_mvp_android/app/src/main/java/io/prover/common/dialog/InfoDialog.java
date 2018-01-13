package io.prover.common.dialog;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.ethereum.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

import io.prover.common.util.Etherium;
import io.prover.provermvp.R;

/**
 * Created by babay on 15.11.2017.
 */

public class InfoDialog extends Dialog implements View.OnClickListener {

    private final TextView addressView;
    private final DialogActionsListener actionsListener;
    private String address;

    public InfoDialog(@NonNull Context context, DialogActionsListener actionsListener) {
        super(context);
        this.actionsListener = actionsListener;
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_info);
        addressView = findViewById(R.id.addressView);

        updateAddress();
        addressView.setOnClickListener(this);
        findViewById(R.id.buttonOk).setOnClickListener(this);
        findViewById(R.id.buttonExport).setOnClickListener(this);
        findViewById(R.id.buttonImport).setOnClickListener(this);
        setTitle(R.string.info);

        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.TOP;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    private void updateAddress() {
        Etherium etherium = Etherium.getInstance(getContext());
        ECKey key = etherium.getKey();
        if (key != null) {
            byte[] addressBytes = key.getAddress();
            byte[] addDigitBytes = Hex.encode(addressBytes);
            address = "0x" + new String(addDigitBytes);
            addressView.setText(address);
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

            case R.id.buttonImport:
                dismiss();
                actionsListener.showImportDialog();
                break;

            case R.id.buttonExport:
                dismiss();
                actionsListener.showExportDialog();
                break;
        }
    }

    public interface DialogActionsListener {
        void showExportDialog();

        void showImportDialog();
    }
}
