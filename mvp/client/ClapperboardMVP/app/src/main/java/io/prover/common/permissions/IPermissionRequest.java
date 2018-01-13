package io.prover.common.permissions;

import android.app.Activity;

/**
 * Created by babay on 01.09.2017.
 */

public interface IPermissionRequest {

    /**
     * check permissions granted
     *
     * @param activity
     * @param requestCode
     * @param dialogStyle
     * @return permissions that are not granted
     */
    IPermissionRequest check(final Activity activity, final int requestCode, int dialogStyle);

    /**
     * set callback to be run after permissions granted
     *
     * @param runAfterGrant
     * @return
     */
    IPermissionRequest runAfterGrant(IRunAfterPermissionsGranted runAfterGrant);

    IPermissionRequest addExplainer(IPermissionExplainer explainer);

    boolean hasPermission(Activity activity);

    void explainOrRequest(Activity activity, final int requestCode, int dialogStyle);

    void request(Activity activity, final int requestCode);
}
