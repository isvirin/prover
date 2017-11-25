package io.prover.provermvp.gl.prog;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.Buffer;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by babay on 24.11.2017.
 */

public class ReadCameraProgram extends GlProgram {
    protected String FRAGMENT_SHADER = "camera.frag.glsl";
    protected String VERTEX_SHADER = "camera.vert.glsl";

    private int camTextureName;
    private int camTextureTransformLocation;
    private int camTexCoordinateLocation;
    private int positionLocation;
    private int camTextureLocation;

    public ReadCameraProgram() {

    }

    public void load(Context context) {
        load(context, VERTEX_SHADER, FRAGMENT_SHADER);

        camTextureLocation = GLES20.glGetUniformLocation(programName, "camTexture");
        camTextureTransformLocation = GLES20.glGetUniformLocation(programName, "camTextureTransform");
        camTexCoordinateLocation = GLES20.glGetAttribLocation(programName, "camTexCoordinate");
        positionLocation = GLES20.glGetAttribLocation(programName, "position");
    }

    @Override
    public void bind() {
    }

    public void bind(int cameraTexture, float[] cameraTransformmatrix, Buffer texCoords, Buffer positionBuffer) {
        glUseProgram(programName);

        glActiveTexture(GL_TEXTURE0);//GLES20.GL_TEXTURE0
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture);
        glUniform1i(camTextureLocation, 0);

        glUniformMatrix4fv(camTextureTransformLocation, 1, false, cameraTransformmatrix, 0);

        glEnableVertexAttribArray(camTexCoordinateLocation);
        glVertexAttribPointer(camTexCoordinateLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, texCoords);

        glEnableVertexAttribArray(positionLocation);
        glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, positionBuffer);
    }

    @Override
    public void unbind() {
        GLES20.glDisableVertexAttribArray(camTexCoordinateLocation);
        GLES20.glDisableVertexAttribArray(positionLocation);
        GLES20.glUseProgram(0);
    }

}
