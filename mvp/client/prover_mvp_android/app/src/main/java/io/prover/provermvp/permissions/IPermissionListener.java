package io.prover.provermvp.permissions;

/**
 * Created by babay on 01.09.2017.
 */

public interface IPermissionListener {
    void onGranted(PermissionRequest request, boolean alreadyHadPermissions);

    void onRejected(PermissionRequest request);
}
