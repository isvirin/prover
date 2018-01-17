package io.prover.common.permissions;

        import android.app.Activity;
        import android.support.annotation.NonNull;

        import java.util.ArrayList;
        import java.util.List;

        import io.prover.clapperboardmvp.R;
        import io.prover.common.Const;

        import static android.Manifest.permission.CAMERA;
        import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
        import static android.Manifest.permission.RECORD_AUDIO;
        import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by babay on 30.09.2016.
 */

public class PermissionManager {

    private static final List<PermissionRequestSet> expectedPermissions = new ArrayList<>();

    private static int nextFreeId = 1000;

    public static boolean ensureHaveWriteSdcardPermission(final Activity activity, final IRunAfterPermissionsGranted runAfterGrant) {
        return ensureHavePermissions(activity, getRequest(WRITE_EXTERNAL_STORAGE, runAfterGrant));
    }

    public static boolean ensureHaveReadSdcardPermission(final Activity activity, final IRunAfterPermissionsGranted runAfterGrant) {
        return ensureHavePermissions(activity, getRequest(READ_EXTERNAL_STORAGE, runAfterGrant));
    }

    public static boolean ensureHaveCameraPermission(final Activity activity, final IRunAfterPermissionsGranted runAfterGrant) {
        return ensureHavePermissions(activity, getRequest(CAMERA, runAfterGrant));
    }

    public static boolean checkHaveCameraPermission(final Activity activity) {
        return checkHavePermissions(activity, getRequest(CAMERA, null));
    }

    public static boolean checkHaveWriteSdcardPermission(final Activity activity) {
        return checkHavePermissions(activity, getRequest(WRITE_EXTERNAL_STORAGE, null));
    }

    public static boolean ensureHavePermissions(final Activity activity, PermissionRequestSet set) {
        set = set.check(activity, ++nextFreeId, 0);
        if (set.size() == 0)
            return true;
        expectedPermissions.add(set);
        return false;
    }

    public static boolean checkHavePermissions(final Activity activity, PermissionRequestSet set) {
        set = set.check(activity, ++nextFreeId, 0);
        return set.size() == 0;
    }

    public static void onPermissionRequestDone(Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < expectedPermissions.size(); i++) {
            PermissionRequestSet set = expectedPermissions.get(i);
            if (set.getRequestCode() == requestCode) {
                set.onPermissionRequestDone(activity, Const.REQUEST_CODE_FOR_REQUEST_PERMISSIONS, permissions, grantResults);
                expectedPermissions.remove(i--);
            }
        }
    }

    private static PermissionRequestSet getRequest(String permission, final IRunAfterPermissionsGranted runAfterGrant) {
        switch (permission) {
            case WRITE_EXTERNAL_STORAGE:
                return new PermissionRequestSet(WRITE_EXTERNAL_STORAGE)
                        .addExplainer(new SimplePermissionExplainer(R.string.permissionRequired, R.string.writePermissionRequired))
                        .runAfterGrant(runAfterGrant);

            case READ_EXTERNAL_STORAGE:
                return new PermissionRequestSet(READ_EXTERNAL_STORAGE)
                        .addExplainer(new SimplePermissionExplainer(R.string.permissionRequired, R.string.readPermissionRequired))
                        .runAfterGrant(runAfterGrant);

            case CAMERA:
                return new PermissionRequestSet(CAMERA, RECORD_AUDIO)
                        .runAfterGrant(runAfterGrant);


            default:
                throw new RuntimeException("not implementer for permission " + permission);
        }
    }


}
