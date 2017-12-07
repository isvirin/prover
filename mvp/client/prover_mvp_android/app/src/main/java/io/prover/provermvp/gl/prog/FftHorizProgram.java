package io.prover.provermvp.gl.prog;

import android.content.Context;
import android.opengl.GLES20;

import io.prover.provermvp.camera.Size;
import io.prover.provermvp.gl.TexRect;
import io.prover.provermvp.gl.Texture;
import io.prover.provermvp.gl.TurnCoefficientTexture;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static java.lang.Math.PI;

/**
 * Created by babay on 05.12.2017.
 */

public class FftHorizProgram extends GlProgram {
    protected String FRAGMENT_SHADER = "fft_horiz.frag.glsl";
    protected String VERTEX_SHADER = "copy_scaled.vert.glsl";

    private int texCoordinateLocation;
    private int texTurnCoeffLocation;
    private int positionLocation;
    private int textureLocation;
    private int texStepLocation;
    private int intStepLocation;
    private int turnCoeffRowLocation;
    private int widthLocation;
    private int heightLocation;
    private int argMulLocation;

    @Override
    public void load(Context context) {
        load(context, VERTEX_SHADER, FRAGMENT_SHADER);

        textureLocation = GLES20.glGetUniformLocation(programName, "s_texture");
        texTurnCoeffLocation = GLES20.glGetUniformLocation(programName, "s_turnCoeff_texture");

        texStepLocation = GLES20.glGetUniformLocation(programName, "texStep");
        intStepLocation = GLES20.glGetUniformLocation(programName, "intStep");
        turnCoeffRowLocation = GLES20.glGetUniformLocation(programName, "turnCoeffRow");
        argMulLocation = GLES20.glGetUniformLocation(programName, "argMul");

        widthLocation = GLES20.glGetUniformLocation(programName, "width");
        heightLocation = GLES20.glGetUniformLocation(programName, "height");

        texCoordinateLocation = GLES20.glGetAttribLocation(programName, "texCoordinate");
        positionLocation = GLES20.glGetAttribLocation(programName, "position");
    }

    public void bind(TexRect texRect, Size size) {
        glUseProgram(programName);
        glUniform1i(textureLocation, 0);

        glUniform1f(widthLocation, size.width);
        glUniform1f(heightLocation, size.height);

        glEnableVertexAttribArray(texCoordinateLocation);
        glVertexAttribPointer(texCoordinateLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, texRect.textureBuffer);

        glEnableVertexAttribArray(positionLocation);
        glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, texRect.vertexBuffer);
    }

    public void bind(Texture sourceTexture, TurnCoefficientTexture turnCoefficientTexture, TexRect texRect, Size size, int step) {
        int intStep = 1 << step;
        float texStep = intStep / (float) size.width;
        float turnCoeffRow = (0.5f + step - 1) / turnCoefficientTexture.size.height;
        float argMul = (float) (-2 * PI / (2 << step));

        glUseProgram(programName);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, sourceTexture.texId);
        //glActiveTexture(GL_TEXTURE1);
        //glBindTexture(GL_TEXTURE_2D, turnCoefficientTexture.texId);
        glUniform1i(textureLocation, 0);
        //glUniform1i(texTurnCoeffLocation, 1);

        glUniform1f(texStepLocation, texStep);
        glUniform1f(intStepLocation, intStep);
        glUniform1f(turnCoeffRowLocation, turnCoeffRow);
        glUniform1f(widthLocation, size.width);
        glUniform1f(heightLocation, size.height);
        glUniform1f(argMulLocation, argMul);

        glEnableVertexAttribArray(texCoordinateLocation);
        glVertexAttribPointer(texCoordinateLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, texRect.textureBuffer);

        glEnableVertexAttribArray(positionLocation);
        glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, texRect.vertexBuffer);
    }

    public void updateBinding(Texture sourceTexture, Size size, int step) {
        int intStep = 1 << step;
        float texStep = intStep / (float) size.width;
        float argMul = (float) (-2 * PI / (2 << step));

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, sourceTexture.texId);

        glUniform1f(texStepLocation, texStep);
        glUniform1f(intStepLocation, intStep);
        glUniform1f(argMulLocation, argMul);
    }

    @Override
    public void unbind() {
        GLES20.glDisableVertexAttribArray(texCoordinateLocation);
        GLES20.glDisableVertexAttribArray(positionLocation);
        GLES20.glUseProgram(0);
    }


}
