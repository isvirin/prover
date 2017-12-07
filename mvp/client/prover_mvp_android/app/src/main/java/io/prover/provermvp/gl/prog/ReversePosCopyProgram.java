package io.prover.provermvp.gl.prog;

import android.content.Context;
import android.opengl.GLES20;

import io.prover.provermvp.gl.TexRect;
import io.prover.provermvp.gl.Texture;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;

/**
 * Created by babay on 24.11.2017.
 */

public class ReversePosCopyProgram extends GlProgram {
    protected String FRAGMENT_SHADER = "reverse_copy.frag.glsl";
    protected String VERTEX_SHADER = "copy_scaled.vert.glsl";

    private int texCoordinateLocation;
    private int positionLocation;
    private int textureLocation;
    private int widthLocation;
    private int heightLocation;
    private int pxStepXLocation;
    private int pxStepYLocation;
    private int revOrderTable;
    private int usedHiBitPart;
    private int unusedHiBitPart;

    public ReversePosCopyProgram() {

    }

    public void load(Context context) {
        load(context, VERTEX_SHADER, FRAGMENT_SHADER);

        textureLocation = GLES20.glGetUniformLocation(programName, "s_texture");
        texCoordinateLocation = GLES20.glGetAttribLocation(programName, "texCoordinate");
        positionLocation = GLES20.glGetAttribLocation(programName, "position");
        revOrderTable = GLES20.glGetUniformLocation(programName, "revOrderTable");

        widthLocation = GLES20.glGetUniformLocation(programName, "width");
        heightLocation = GLES20.glGetUniformLocation(programName, "height");
        pxStepXLocation = GLES20.glGetUniformLocation(programName, "pxStepX");
        pxStepYLocation = GLES20.glGetUniformLocation(programName, "pxStepY");
        usedHiBitPart = GLES20.glGetUniformLocation(programName, "usedHiBitPart");
        unusedHiBitPart = GLES20.glGetUniformLocation(programName, "unusedHiBitPart");
    }

    public void bind(Texture sourceTexture, TexRect texRect, int width, int height, int[] revTable) {
        glUseProgram(programName);

        glActiveTexture(GL_TEXTURE0);//GLES20.GL_TEXTURE0
        glBindTexture(GL_TEXTURE_2D, sourceTexture.texId);
        glUniform1i(textureLocation, 0);

        glUniform1f(widthLocation, width);
        glUniform1f(heightLocation, height);
        glUniform1f(pxStepXLocation, 0.5f / width);
        glUniform1f(pxStepYLocation, 0.5f / height);
        glUniform1i(usedHiBitPart, 4);
        glUniform1i(unusedHiBitPart, 64);

        texRect.bindToProgram(texCoordinateLocation, positionLocation);

        GLES20.glUniform1iv(revOrderTable, revTable.length, revTable, 0);
    }

    @Override
    public void unbind() {
        GLES20.glDisableVertexAttribArray(texCoordinateLocation);
        GLES20.glDisableVertexAttribArray(positionLocation);
        GLES20.glUseProgram(0);
    }

}
