package io.prover.provermvp.viewholder;

import android.app.Activity;

import io.prover.provermvp.camera.Size;

/**
 * Created by babay on 08.11.2017.
 */

public interface ICameraViewHolder {
    boolean startRecording(Activity activity);

    void finishRecording();

    void cancelRecording();

    void onPause(Activity mainActivity);

    void onResume(Activity mainActivity);

    void onStop();

    void setCameraResolution(Size size);
}
