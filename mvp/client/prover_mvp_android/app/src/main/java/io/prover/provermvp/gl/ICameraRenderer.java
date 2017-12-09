package io.prover.provermvp.gl;

import android.graphics.SurfaceTexture;

import io.prover.provermvp.camera.Size;

/**
 * Created by babay on 09.12.2017.
 */

public interface ICameraRenderer {
    void setFrameSize(Size size);

    void shutdown();

    RenderHandler getRenderHandler();

    SurfaceTexture getInputTexture();

    void setOnRendererReadyListener(OnRendererReadyListener listener);

    void start(int rotationAngle);
}
