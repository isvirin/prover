package io.prover.common.dialog;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.ethereum.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.util.Locale;

import io.prover.clapperboardmvp.R;
import io.prover.common.util.Etherium;
import io.prover.common.util.Util;
import io.prover.common.util.UtilFile;

/**
 * Created by babay on 15.11.2017.
 */

public class ExportDialog extends Dialog implements View.OnClickListener {

    private final TextView addressView;
    private final TextInputLayout passwordinputLayout;
    private final Handler handler = new Handler();
    private final View progress;
    private final View someActivityView;
    private String address;

    public ExportDialog(@NonNull Context context, View someActivityView) {
        super(context);
        this.someActivityView = someActivityView;
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_export);
        addressView = findViewById(R.id.addressView);
        passwordinputLayout = findViewById(R.id.passwordView);
        progress = findViewById(R.id.progressBar);

        updateAddress();
        addressView.setOnClickListener(this);
        findViewById(R.id.buttonOk).setOnClickListener(this);
        findViewById(R.id.buttonCancel).setOnClickListener(this);
        setTitle(R.string.info);

        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.TOP;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.width = ViewGroup.LayoutParams.MATCH_PARENT;

        handler.postDelayed(() -> passwordinputLayout.getEditText().requestFocus(), 1000);
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
                String password = passwordinputLayout.getEditText().getText().toString();
                if (password.length() > 1) {
                    progress.setVisibility(View.VISIBLE);
                    Util.hideKeyboard(progress);
                    Runnable r = () -> {
                        Etherium etherium = Etherium.getInstance(getContext());
                        ECKey key = etherium.getKey();
                        File dir = Environment.getExternalStorageDirectory();
                        String address = Hex.toHexString(key.getAddress());
                        String name = String.format(Locale.US, "Wallet_%s", address);
                        File file = new File(dir, name);
                        try {
                            Etherium.getInstance(getContext()).exportWallet(file, password);
                            handler.post(() -> {
                                MediaScannerConnection.scanFile(getContext(), new String[]{file.getAbsolutePath()}, null, null);
                                String title = "Wallet 0x" + address;
                                Snackbar.make(someActivityView, "Exported key to file: " + file.getPath(), Snackbar.LENGTH_LONG)
                                        .setAction("Share", v1 -> new UtilFile(file).sendShareIntent(getContext(), title, title))
                                        .show();
                                dismiss();
                            });
                        } catch (Exception e) {
                            handler.post(() -> {
                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                progress.setVisibility(View.GONE);
                            });
                        }
                    };
                    new Thread(r).start();
                }
                break;

            case R.id.buttonCancel:
                dismiss();
        }
    }
}
