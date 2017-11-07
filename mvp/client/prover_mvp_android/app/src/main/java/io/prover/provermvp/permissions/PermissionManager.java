package io.prover.provermvp.permissions;

import android.app.Activity;
import android.support.annotation.NonNull;

import io.prover.provermvp.Const;
import io.prover.provermvp.R;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by babay on 30.09.2016.
 */

public class PermissionManager {

    private static PermissionRequestSet expectedPermissions = new PermissionRequestSet();

    public static boolean ensureHaveWriteSdcardPermission(final Activity activity, final IRunAfterPermissionsGranted runAfterGrant) {
        return ensureHavePermissions(activity, getRequest(WRITE_EXTERNAL_STORAGE, runAfterGrant));
    }

    public static boolean ensureHaveCameraPermission(final Activity activity, final IRunAfterPermissionsGranted runAfterGrant) {
        return ensureHavePermissions(activity, getRequest(CAMERA, runAfterGrant));
    }

    public static boolean ensureHavePermissions(final Activity activity, PermissionRequestSet set) {
        expectedPermissions = set.check(activity, Const.REQUEST_CODE_FOR_REQUEST_PERMISSIONS, 0);
        return expectedPermissions.size() == 0;
    }

    //grantUriPermission()

    public static void onPermissionRequestDone(Activity activity, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (expectedPermissions != null) {
            expectedPermissions.onPermissionRequestDone(activity, Const.REQUEST_CODE_FOR_REQUEST_PERMISSIONS, permissions, grantResults);
        }
    }

    private static PermissionRequestSet getRequest(String permission, final IRunAfterPermissionsGranted runAfterGrant) {
        switch (permission) {
            case WRITE_EXTERNAL_STORAGE:
                return new PermissionRequestSet(WRITE_EXTERNAL_STORAGE)
                        .addExplainer(new SimplePermissionExplainer(R.string.permissionRequired, R.string.writePermissionRequired))
                        .runAfterGrant(runAfterGrant);

            case CAMERA:
                return new PermissionRequestSet(CAMERA, RECORD_AUDIO)
                        .runAfterGrant(runAfterGrant);


            default:
                throw new RuntimeException("not implementer for permission " + permission);
        }
    }
}
