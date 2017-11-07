package io.prover.provermvp.permissions;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

/**
 * Created by babay on 11.05.2017.
 */

public class PermissionRequestSet extends ArrayList<PermissionRequest> implements IPermissionRequest, IPermissionListener {
    int style;
    IPermissionExplainer explainer;
    List<PermissionRequest> rejected;
    private IRunAfterPermissionsGranted runAfterGrant;

    public PermissionRequestSet() {
    }

    public PermissionRequestSet(PermissionRequest request) {
        add(request);
    }

    public PermissionRequestSet(String... permissions) {
        super(permissions.length);
        for (String permission : permissions) {
            add(new PermissionRequest(permission));
        }
    }

    @Override
    public boolean hasPermission(Activity activity) {
        for (PermissionRequest permissionRequest : this) {
            if (!permissionRequest.hasPermission(activity)) {
                return false;
            }
        }
        return true;
    }

    /**
     * checks permissions
     *
     * @param activity
     * @param requestCode
     * @param dialogStyle
     * @return unavailablePermissions
     */

    @Override
    public PermissionRequestSet check(final Activity activity, final int requestCode, int dialogStyle) {
        this.style = dialogStyle;

        for (PermissionRequest permissionRequest : new ArrayList<>(this)) {
            if (permissionRequest.hasPermission(activity)) {
                permissionRequest.onGranted(true);
            }
        }

        if (size() == 0) {
            return this;
        }

        PermissionRequestSet permissionsToExplain = new PermissionRequestSet();
        PermissionRequestSet permissionsToRequest = new PermissionRequestSet();
        for (Iterator<PermissionRequest> iterator = iterator(); iterator.hasNext(); ) {
            PermissionRequest permissionRequest = iterator.next();
            if (permissionRequest.shouldShowPermissionExplanation(activity))
                permissionsToExplain.add(permissionRequest);
            else
                permissionsToRequest.add(permissionRequest);
        }

        if (permissionsToRequest.size() > 0) {
            for (PermissionRequest request : permissionsToRequest) {
                request.onRequestingPermissions();
            }
            ActivityCompat.requestPermissions(activity, permissionCodes(), requestCode);
        }

        if (permissionsToExplain.size() > 0) {
            permissionsToExplain.explainOrRequest(activity, requestCode, dialogStyle);
        }

        return this;
    }

    private String[] permissionCodes() {
        String[] permissionCodes = new String[size()];
        for (int i = 0; i < this.size(); i++) {
            permissionCodes[i] = this.get(i).permission;

        }
        return permissionCodes;
    }

    @Override
    public void explainOrRequest(Activity activity, int requestCode, int dialogStyle) {
        if (explainer != null) {
            explainer.setPositiveListener((dialog, which) -> {
                dialog.dismiss();
                request(activity, requestCode);
            }).show(activity, dialogStyle);
        } else {
            for (PermissionRequest permissionRequest : this) {
                permissionRequest.check(activity, requestCode, dialogStyle);
            }
        }
    }

    @Override
    public void request(Activity activity, int requestCode) {
        for (PermissionRequest permissionRequest : this) {
            permissionRequest.request(activity, requestCode);
        }
    }

    @Override
    public boolean add(PermissionRequest permissionRequest) {
        permissionRequest.addListener(this);
        return super.add(permissionRequest);
    }

    @Override
    public void add(int index, PermissionRequest element) {
        element.addListener(this);
        super.add(index, element);
    }

    public void onPermissionRequestDone(Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < grantResults.length; i++) {
            PermissionRequest request = getRequestForPermission(permissions[i]);
            if (request != null)
                if (grantResults[i] != PERMISSION_GRANTED) {
                    request.onFailed(activity);
                    if (request.isAbsolutelyRequired() && request.canRequestNow()) {
                        request.check(activity, requestCode, style);
                    }
                } else {
                    request.onGranted(false);
                }
        }

    }

    public PermissionRequest getRequestForPermission(String permission) {
        for (PermissionRequest request : this) {
            if (request.permission.equals(permission))
                return request;
        }
        return null;
    }

    @Override
    public PermissionRequestSet addExplainer(IPermissionExplainer explainer) {
        this.explainer = explainer;
        return this;
    }

    @Override
    public PermissionRequestSet runAfterGrant(IRunAfterPermissionsGranted runAfterGrant) {
        this.runAfterGrant = runAfterGrant;
        return this;
    }

    @Override
    public void onGranted(PermissionRequest request, boolean alreadyHadPermissions) {
        remove(request);
        if (rejected != null)
            rejected.remove(request);
        if (size() == 0 && runAfterGrant != null) {
            runAfterGrant.onAfterPermissionsGranted();
        }
    }

    @Override
    public void onRejected(PermissionRequest request) {
        if (rejected == null)
            rejected = new ArrayList<>();
        if (!rejected.contains(request)) {
            rejected.add(request);
        }
    }
}
