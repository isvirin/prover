package io.prover.common.permissions;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Created by babay on 30.01.2017.
 */

public class PermissionRequest implements IPermissionRequest {

    public final String permission;
    final String[] permissions;
    private final List<IPermissionListener> permissionListeners = new ArrayList<>();
    IPermissionExplainer explainer;
    private Runnable onPermissionFailedCallback;
    private IRunAfterPermissionsGranted onPermissionGrantedCallback;
    private long requestPermissionsTime;
    private boolean absolutelyRequired = false;
    private int showCounter;

    public PermissionRequest(String permission) {
        this.permissions = new String[]{permission};
        this.permission = permission;
    }

    @Override
    public PermissionRequest check(final Activity activity, final int requestCode, int dialogStyle) {
        if (hasPermission(activity)) {
            onGranted(true);
            return null;
        }

        if (shouldShowPermissionExplanation(activity)) {
            explainOrRequest(activity, requestCode, dialogStyle);
        } else {
            request(activity, requestCode);
        }
        return this;
    }

    @Override
    public void explainOrRequest(Activity activity, final int requestCode, int dialogStyle) {
        if (explainer != null) {
            explainer.setPositiveListener((dialog, which) -> {
                onRequestingPermissions();
                ActivityCompat.requestPermissions(activity, permissions, requestCode);
                dialog.dismiss();
            }).show(activity, dialogStyle);
        } else {
            request(activity, requestCode);
        }
    }

    @Override
    public void request(Activity activity, int requestCode) {
        onRequestingPermissions();
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public boolean hasPermission(Activity activity) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public boolean shouldShowPermissionExplanation(Activity activity) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
                return true;
        }
        return false;
    }

    public void onRequestingPermissions() {
        requestPermissionsTime = System.currentTimeMillis();
        showCounter--;
    }

    public void onGranted(boolean alreadyHadPermissions) {
        for (IPermissionListener permissionListener : permissionListeners) {
            permissionListener.onGranted(this, alreadyHadPermissions);
        }

        if (onPermissionGrantedCallback != null) {
            onPermissionGrantedCallback.onAfterPermissionsGranted();
        }
    }

    public void addListener(IPermissionListener listener) {
        permissionListeners.add(listener);
    }

    public void onFailed(Context context) {
        for (IPermissionListener permissionListener : permissionListeners) {
            permissionListener.onRejected(this);
        }

        if (onPermissionFailedCallback != null) {
            onPermissionFailedCallback.run();
        }
        if (explainer != null) {
            Toast.makeText(context, explainer.getExplanationAfterReject(context), Toast.LENGTH_SHORT).show();
        }
    }

    public PermissionRequest setOnPermissionFailedCallback(Runnable onPermissionFailedCallback) {
        this.onPermissionFailedCallback = onPermissionFailedCallback;
        return this;
    }

    @Override
    public PermissionRequest runAfterGrant(IRunAfterPermissionsGranted runAfterGrant) {
        this.onPermissionGrantedCallback = runAfterGrant;
        return this;
    }

    public boolean canRequestNow() {
        return System.currentTimeMillis() - requestPermissionsTime > 500 && showCounter > 0;
    }

    public boolean isAbsolutelyRequired() {
        return absolutelyRequired;
    }

    public PermissionRequest setAbsolutelyRequired(boolean absolutelyRequired) {
        this.absolutelyRequired = absolutelyRequired;
        if (absolutelyRequired)
            showCounter = 2;
        return this;
    }

    @Override
    public PermissionRequest addExplainer(IPermissionExplainer explainer) {
        this.explainer = explainer;
        return this;
    }
}
