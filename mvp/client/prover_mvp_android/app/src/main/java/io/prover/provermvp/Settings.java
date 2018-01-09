package io.prover.provermvp;

/**
 * Created by babay on 15.11.2017.
 */

public interface Settings {
    boolean REQUEST_SWYPE = true || !BuildConfig.DEBUG;
    boolean REUSE_PREVIEW_BUFFERS = true;
    boolean USE_CAMERA_2 = true;

    boolean FAKE_SWYPE_CODE = BuildConfig.DEBUG;

    boolean SHOW_RENDERER_PREVIEW = false;

    boolean SHOW_DEFECT = false;
    boolean ADD_SWYPE_CODE_TO_FILE_NAME = true;
}
