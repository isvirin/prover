package io.prover.clapperboardmvp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.lang.ref.WeakReference;

import io.prover.clapperboardmvp.controller.Controller;
import io.prover.clapperboardmvp.viewholder.ControlsViewHolder;
import io.prover.common.dialog.ExportDialog;
import io.prover.common.dialog.ImportDialog;
import io.prover.common.dialog.InfoDialog;
import io.prover.common.permissions.PermissionManager;

import static android.content.Intent.EXTRA_LOCAL_ONLY;
import static io.prover.common.Const.REQUEST_CODE_FOR_IMPORT_WALLET;

public class MainActivity extends AppCompatActivity implements InfoDialog.DialogActionsListener, ImportDialog.ImportFileListener {

    Controller controller;
    WeakReference<ImportDialog> importDialogRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        controller = new Controller(this);
        new ControlsViewHolder(this, controller);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        controller.onResume();
    }

    @Override
    protected void onPause() {
        controller.onPause();
        super.onPause();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        new Handler().post(() -> PermissionManager.onPermissionRequestDone(this, requestCode, permissions, grantResults));
    }

    @Override
    public void showExportDialog() {
        PermissionManager.ensureHaveWriteSdcardPermission(this, () -> {
            new ExportDialog(this, findViewById(R.id.contentRoot)).show();
        });
    }

    @Override
    public void showImportDialog() {
        PermissionManager.ensureHaveReadSdcardPermission(this, () -> {
            ImportDialog importDialog = new ImportDialog(this, controller, this);
            importDialog.show();
            importDialogRef = new WeakReference<>(importDialog);
        });
    }

    @Override
    public void onRequestImportFile(String currentPath) {
        Intent intent = new Intent()
                .setType("file/*")
                .setAction(Intent.ACTION_GET_CONTENT)
                .putExtra(EXTRA_LOCAL_ONLY, true);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), REQUEST_CODE_FOR_IMPORT_WALLET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_FOR_IMPORT_WALLET:
                if (resultCode == RESULT_OK) {
                    Uri selectedfile = data.getData();
                    if (selectedfile == null || selectedfile.getPath() == null)
                        return;
                    ImportDialog importDialog = importDialogRef.get();
                    if (importDialog != null) {
                        importDialog.setFilePath(selectedfile.getPath());
                    } else {
                        showImportDialog();
                        importDialog = importDialogRef.get();
                        importDialog.setFilePath(selectedfile.getPath());
                    }
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
