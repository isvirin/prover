package io.prover.provermvp.gl;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import io.prover.provermvp.gl.lib.GlUtil;

/**
 * Created by babay on 01.12.2017.
 */

public class CameraTexture extends Texture {

    /**
     * matrix for transforming our camera texture, available immediately after {@link #mCameraInputTexture}s
     * {@code updateTexImage()} is called in our main {@link CameraRenderer#draw()} loop.
     */
    public final float[] mCameraTransformMatrix = new float[16];
    public final float[] mScreenTransformMatrix = new float[16];
    public SurfaceTexture mCameraInputTexture;

    public CameraTexture() {
    }

    public void init(int texId, SurfaceTexture.OnFrameAvailableListener listener) {
        this.texId = texId;
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texId);
        GlUtil.checkGlError2("Texture bind");

        mCameraInputTexture = new SurfaceTexture(texId);
        mCameraInputTexture.setOnFrameAvailableListener(listener);
    }

    public void deInit() {
        mCameraInputTexture.release();
        mCameraInputTexture.setOnFrameAvailableListener(null);
    }

    /**
     * update the SurfaceTexture to the latest camera image
     */
    protected void updatePreviewTexture() {
        mCameraInputTexture.updateTexImage();
        mCameraInputTexture.getTransformMatrix(mCameraTransformMatrix);
    }

    public void setScreenRotation(int rotationAngle) {
        Matrix.setIdentityM(mScreenTransformMatrix, 0);
        Matrix.rotateM(mScreenTransformMatrix, 0, rotationAngle, 0, 0, 1);
    }
}
