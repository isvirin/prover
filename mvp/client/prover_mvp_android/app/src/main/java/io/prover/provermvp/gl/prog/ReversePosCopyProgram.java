package io.prover.provermvp.gl.prog;

import android.content.Context;
import android.opengl.GLES20;

import io.prover.provermvp.gl.TexRect;
import io.prover.provermvp.gl.Texture;

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

public class ReversePosCopyProgram extends GlProgram {
    protected String FRAGMENT_SHADER = "reverse_copy.frag.glsl";
    protected String VERTEX_SHADER = "copy.vert.glsl";

    private int texCoordinateLocation;
    private int positionLocation;
    private int textureLocation;
    private int revOrderTable;

    public ReversePosCopyProgram() {

    }

    public void load(Context context) {
        load(context, VERTEX_SHADER, FRAGMENT_SHADER);

        textureLocation = GLES20.glGetUniformLocation(programName, "s_texture");
        texCoordinateLocation = GLES20.glGetAttribLocation(programName, "texCoordinate");
        positionLocation = GLES20.glGetAttribLocation(programName, "position");
        revOrderTable = GLES20.glGetUniformLocation(programName, "revOrderTable");
    }

    @Override
    public void bind() {
    }

    public void bind(Texture sourceTexture, TexRect texRect, int[] revTable) {
        glUseProgram(programName);

        glActiveTexture(GL_TEXTURE0);//GLES20.GL_TEXTURE0
        glBindTexture(GL_TEXTURE_2D, sourceTexture.texId);
        glUniform1i(textureLocation, 0);

        glEnableVertexAttribArray(texCoordinateLocation);
        glVertexAttribPointer(texCoordinateLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, texRect.textureBuffer);

        glEnableVertexAttribArray(positionLocation);
        glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, texRect.vertexBuffer);

        GLES20.glUniform1iv(revOrderTable, revTable.length, revTable, 0);
    }

    @Override
    public void unbind() {
        GLES20.glDisableVertexAttribArray(texCoordinateLocation);
        GLES20.glDisableVertexAttribArray(positionLocation);
        GLES20.glUseProgram(0);
    }

}
