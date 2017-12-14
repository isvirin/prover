package io.prover.provermvp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.dialog.InfoDialog;
import io.prover.provermvp.permissions.PermissionManager;
import io.prover.provermvp.viewholder.BalanceStatusHolder;
import io.prover.provermvp.viewholder.CameraControlsHolder;
import io.prover.provermvp.viewholder.CameraViewHolder;
import io.prover.provermvp.viewholder.CameraViewHolder2;
import io.prover.provermvp.viewholder.ICameraViewHolder;
import io.prover.provermvp.viewholder.ScreenLogger;
import io.prover.provermvp.viewholder.SwypeStateHelperHolder;
import io.prover.provermvp.viewholder.SwypeViewHolder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final Handler handler = new Handler();
    SwypeStateHelperHolder swypeStateHelperHolder;
    private CameraController cameraController;
    private ICameraViewHolder cameraHolder;
    private CameraControlsHolder cameraControlsHolder;
    private boolean resumed;
    private boolean started;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraController = new CameraController(this);
        setContentView(R.layout.activity_main);
        FrameLayout cameraContainer = findViewById(R.id.cameraContainer);
        ConstraintLayout contentRoot = findViewById(R.id.contentRoot);

        if (Settings.USE_CAMERA_2)
            cameraHolder = new CameraViewHolder2(contentRoot, this, cameraController);
        else
            cameraHolder = new CameraViewHolder(this, cameraContainer, cameraController);

        cameraControlsHolder = new CameraControlsHolder(this, contentRoot, cameraHolder, cameraController);
        swypeStateHelperHolder = new SwypeStateHelperHolder(contentRoot, cameraController);
        new BalanceStatusHolder(contentRoot, cameraController);
        new SwypeViewHolder(findViewById(R.id.swypeView), cameraController);

        if (Settings.ENABLE_SCREEN_LOG) {
            ScreenLogger logger = new ScreenLogger(contentRoot);
            cameraController.setScreenLogger(logger);
        }
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
    protected void onPause() {
        super.onPause();
        cameraController.onPause();
        cameraHolder.onPause(this);
        resumed = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraController.onResume();
        cameraHolder.onResume(this);
        resumed = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        started = true;
        cameraControlsHolder.onStart();
    }

    @Override
    protected void onStop() {
        started = false;
        super.onStop();
        cameraControlsHolder.onStop();
        cameraHolder.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        handler.post(() -> PermissionManager.onPermissionRequestDone(this, requestCode, permissions, grantResults));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.infoButton:
                new InfoDialog(this).show();
                break;
        }
    }
}
