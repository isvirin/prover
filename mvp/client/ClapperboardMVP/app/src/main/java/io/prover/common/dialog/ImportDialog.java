package io.prover.common.dialog;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.ethereum.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.InputStream;

import io.prover.clapperboardmvp.R;
import io.prover.clapperboardmvp.controller.Controller;
import io.prover.common.util.Etherium;
import io.prover.common.util.PathUtil;
import io.prover.common.util.Util;
import io.prover.common.wallet.ECKeyPair;
import io.prover.common.wallet.Wallet;
import io.prover.common.wallet.WalletFile;

/**
 * Created by babay on 15.11.2017.
 */

public class ImportDialog extends Dialog implements View.OnClickListener {

    private final TextView addressView;
    private final TextInputLayout passwordInputLayout;
    private final TextInputLayout fileInputLayout;
    private final Button okButton;
    private final View addressLabel;

    private final Handler handler = new Handler();
    private final View progress;
    private final ImportFileListener importFileListener;
    private final Controller controller;
    private ECKey keyPair;
    private Uri fileUri;
    private String filePath;

    public ImportDialog(@NonNull Context context, Controller controller, ImportFileListener importFileListener) {
        super(context);
        this.controller = controller;
        this.importFileListener = importFileListener;
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_import);
        addressView = findViewById(R.id.addressView);
        passwordInputLayout = findViewById(R.id.passwordView);
        fileInputLayout = findViewById(R.id.fileInput);
        progress = findViewById(R.id.progressBar);
        okButton = findViewById(R.id.buttonOk);
        addressLabel = findViewById(R.id.addressLabel);

        addressView.setOnClickListener(this);
        findViewById(R.id.buttonOk).setOnClickListener(this);
        findViewById(R.id.buttonCancel).setOnClickListener(this);
        findViewById(R.id.chooseFileButton).setOnClickListener(this);
        setTitle(R.string.info);

        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.TOP;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addressView:
                String address = addressView.getText().toString();
                if (address.length() > 0) {
                    Context context = v.getContext();
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(address, address);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, R.string.addressCopied, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.buttonOk:
                if (keyPair == null) {
                    String password = passwordInputLayout.getEditText().getText().toString();
                    progress.setVisibility(View.VISIBLE);
                    Util.hideKeyboard(progress);
                    File file = new File(fileInputLayout.getEditText().getText().toString());
                    if (!file.exists()) {
                        Toast.makeText(getContext(), R.string.fileNotExist, Toast.LENGTH_SHORT).show();
                    } else {
                        if (fileUri != null && fileInputLayout.getEditText().getText().toString().equalsIgnoreCase(filePath))
                            openWalletUri(fileUri, password);
                        else
                            openWalletUri(Uri.fromFile(new File(fileInputLayout.getEditText().getText().toString())), password);
                    }
                } else {
                    Etherium.getInstance(getContext()).setKey(keyPair, getContext());
                    controller.networkHolder.doHello();
                    dismiss();
                }
                break;

            case R.id.buttonCancel:
                dismiss();
                break;

            case R.id.chooseFileButton:
                importFileListener.onRequestImportFile(fileInputLayout.getEditText().getText().toString());
                break;
        }
    }

    private void openWalletUri(Uri uri, String password) {
        Runnable r = () -> {
            try {
                InputStream iStream = getContext().getContentResolver().openInputStream(uri);
                WalletFile walletFile = new WalletFile(iStream);
                ECKeyPair ecKeyPair = Wallet.decrypt(password, walletFile);
                keyPair = ECKey.fromPrivate(ecKeyPair.getPrivateKey());
                String address = "0x" + Hex.toHexString(keyPair.getAddress());
                handler.post(() -> {
                    ViewGroup parent = (ViewGroup) addressView.getParent();
                    while (parent.getParent() instanceof ViewGroup) {
                        parent = (ViewGroup) parent.getParent();
                    }
                    TransitionManager.beginDelayedTransition(parent);

                    addressView.setText(address);
                    addressView.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                    okButton.setText(R.string.import_);
                    addressLabel.setVisibility(View.VISIBLE);
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

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
        filePath = PathUtil.getExistingFilePath(getContext(), fileUri);
        if (filePath != null){
            fileInputLayout.getEditText().setText(filePath);
        } else {
            fileInputLayout.getEditText().setText(fileUri.toString());
        }

        resetAddress();
        passwordInputLayout.getEditText().requestFocus();
    }

    void resetAddress() {
        if (addressView.getVisibility() == View.VISIBLE) {
            ViewGroup parent = (ViewGroup) addressView.getParent();
            while (parent.getParent() instanceof ViewGroup) {
                parent = (ViewGroup) parent.getParent();
            }
            TransitionManager.beginDelayedTransition(parent);

            addressView.setVisibility(View.GONE);
            keyPair = null;
            okButton.setText(R.string.open);
            addressLabel.setVisibility(View.GONE);
        }
    }

    public interface ImportFileListener {
        void onRequestImportFile(String currentPath);
    }
}
