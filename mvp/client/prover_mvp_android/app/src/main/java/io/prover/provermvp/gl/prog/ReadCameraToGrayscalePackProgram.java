package io.prover.provermvp.gl.prog;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import io.prover.provermvp.gl.TexRect;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform2f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;

/**
 * Created by babay on 24.11.2017.
 */

public class ReadCameraToGrayscalePackProgram extends GlProgram {
    protected String FRAGMENT_SHADER = "camera_grayscale_pack.frag.glsl";
    protected String VERTEX_SHADER = "camera_grayscale_pack.vert.glsl";
    float[] tempVector1 = new float[]{0, 0, 0, 0};
    float[] tempVector2 = new float[4];
    private int camTextureTransformLocation;
    private int screenTransformLocation;
    private int camTexCoordinateLocation;
    private int positionLocation;
    private int camTextureLocation;
    private int hStepVecLocation;

    public ReadCameraToGrayscalePackProgram() {

    }

    public void load(Context context) {
        load(context, VERTEX_SHADER, FRAGMENT_SHADER);

        camTextureLocation = GLES20.glGetUniformLocation(programName, "camTexture");
        camTextureTransformLocation = GLES20.glGetUniformLocation(programName, "camTextureTransform");
        screenTransformLocation = GLES20.glGetUniformLocation(programName, "screenTransform");
        hStepVecLocation = GLES20.glGetUniformLocation(programName, "hStepVec");
        camTexCoordinateLocation = GLES20.glGetAttribLocation(programName, "camTexCoordinate");
        positionLocation = GLES20.glGetAttribLocation(programName, "position");
    }

    public void bind(int cameraTexture, float[] cameraTransformMatrix, float[] screenTransformMatrix, TexRect texRect, int width) {
        float hStep = 1.0f / width / 4;
        tempVector1[0] = hStep;
        Matrix.multiplyMV(tempVector2, 0, cameraTransformMatrix, 0, tempVector1, 0);

        glUseProgram(programName);

        glActiveTexture(GL_TEXTURE0);//GLES20.GL_TEXTURE0
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture);
        glUniform1i(camTextureLocation, 0);
        glUniform2f(hStepVecLocation, tempVector2[0], tempVector2[1]);

        glUniformMatrix4fv(camTextureTransformLocation, 1, false, cameraTransformMatrix, 0);
        glUniformMatrix4fv(screenTransformLocation, 1, false, screenTransformMatrix, 0);

        texRect.bindToProgram(camTexCoordinateLocation, positionLocation);
    }

    @Override
    public void unbind() {
        GLES20.glDisableVertexAttribArray(camTexCoordinateLocation);
        GLES20.glDisableVertexAttribArray(positionLocation);
        GLES20.glUseProgram(0);
    }
}
