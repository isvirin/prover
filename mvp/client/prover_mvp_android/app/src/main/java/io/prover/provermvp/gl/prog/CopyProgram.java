package io.prover.provermvp.gl.prog;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.Buffer;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by babay on 24.11.2017.
 */

public class CopyProgram extends GlProgram {
    protected String FRAGMENT_SHADER = "copy.frag.glsl";
    protected String VERTEX_SHADER = "copy.vert.glsl";

    private int texCoordinateLocation;
    private int positionLocation;
    private int textureLocation;

    public CopyProgram() {

    }

    public void load(Context context) {
        load(context, VERTEX_SHADER, FRAGMENT_SHADER);

        textureLocation = GLES20.glGetUniformLocation(programName, "s_texture");
        texCoordinateLocation = GLES20.glGetAttribLocation(programName, "texCoordinate");
        positionLocation = GLES20.glGetAttribLocation(programName, "position");
    }

    @Override
    public void bind() {
    }

    public void bind(int sourceTexture, Buffer texCoords, Buffer positionBuffer) {
        glUseProgram(programName);

        glActiveTexture(GL_TEXTURE0);//GLES20.GL_TEXTURE0
        glBindTexture(GL_TEXTURE_2D, sourceTexture);
        glUniform1i(textureLocation, 0);

        glEnableVertexAttribArray(texCoordinateLocation);
        glVertexAttribPointer(texCoordinateLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, texCoords);

        glEnableVertexAttribArray(positionLocation);
        glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, positionBuffer);
    }

    @Override
    public void unbind() {
        GLES20.glDisableVertexAttribArray(texCoordinateLocation);
        GLES20.glDisableVertexAttribArray(positionLocation);
        GLES20.glUseProgram(0);
    }

}
