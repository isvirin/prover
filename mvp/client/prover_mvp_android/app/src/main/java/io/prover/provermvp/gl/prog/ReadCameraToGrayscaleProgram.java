package io.prover.provermvp.gl.prog;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import io.prover.provermvp.gl.TexRect;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;

/**
 * Created by babay on 24.11.2017.
 */

public class ReadCameraToGrayscaleProgram extends GlProgram {
    protected String FRAGMENT_SHADER = "camera_grayscale.frag.glsl";
    protected String VERTEX_SHADER = "grayscale_camera.vert.glsl";

    private int camTextureTransformLocation;
    private int camTexCoordinateLocation;
    private int positionLocation;
    private int camTextureLocation;
    private int revOrderTable;

    public ReadCameraToGrayscaleProgram() {

    }

    public void load(Context context) {
        load(context, VERTEX_SHADER, FRAGMENT_SHADER);

        camTextureLocation = GLES20.glGetUniformLocation(programName, "camTexture");
        camTextureTransformLocation = GLES20.glGetUniformLocation(programName, "camTextureTransform");
        camTexCoordinateLocation = GLES20.glGetAttribLocation(programName, "camTexCoordinate");
        positionLocation = GLES20.glGetAttribLocation(programName, "position");
        revOrderTable = GLES20.glGetUniformLocation(programName, "revOrderTable");
    }

    public void bind(int cameraTexture, float[] cameraTransformMatrix, TexRect texRect, int[] revTable) {
        glUseProgram(programName);

        glActiveTexture(GL_TEXTURE0);//GLES20.GL_TEXTURE0
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture);
        glUniform1i(camTextureLocation, 0);

        glUniformMatrix4fv(camTextureTransformLocation, 1, false, cameraTransformMatrix, 0);

        texRect.bindToProgram(camTexCoordinateLocation, positionLocation);

        GLES20.glUniform1iv(revOrderTable, revTable.length, revTable, 0);
    }

    @Override
    public void unbind() {
        GLES20.glDisableVertexAttribArray(camTexCoordinateLocation);
        GLES20.glDisableVertexAttribArray(positionLocation);
        GLES20.glUseProgram(0);
    }

}
