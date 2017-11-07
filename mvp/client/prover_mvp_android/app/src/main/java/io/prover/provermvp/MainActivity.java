package io.prover.provermvp;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import io.prover.provermvp.permissions.PermissionManager;
import io.prover.provermvp.view.CameraControlsHolder;
import io.prover.provermvp.view.CameraViewHolder;

public class MainActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private CameraViewHolder cameraHolder;
    private CameraControlsHolder cameraControlsHolder;
    private boolean resumed;
    private boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewGroup cameraContainer = findViewById(R.id.cameraContainer);
        cameraHolder = new CameraViewHolder(cameraContainer);

        FloatingActionButton fab = findViewById(R.id.fab);
        cameraControlsHolder = new CameraControlsHolder(this, findViewById(R.id.contentRoot), fab, cameraHolder);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();
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
        cameraHolder.onPause(this);
        cameraControlsHolder.onPause();
        resumed = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraHolder.onResume(this);
        cameraControlsHolder.onResume();
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Const.REQUEST_CODE_FOR_REQUEST_PERMISSIONS:
                handler.post(() -> PermissionManager.onPermissionRequestDone(this, permissions, grantResults));
                break;
        }
    }

}
