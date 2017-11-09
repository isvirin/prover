package io.prover.provermvp.viewholder;

import android.app.Activity;

import java.io.File;

/**
 * Created by babay on 08.11.2017.
 */

public interface ICameraViewHolder {
    boolean isRecording();

    void finishRecording();

    File getVideoFile();

    boolean startRecording(Activity activity);

    void onPause(Activity mainActivity);

    void onResume(Activity mainActivity);

    void onStop();
}
