package io.prover.provermvp.gl.prog;

import android.content.Context;
import android.opengl.GLES20;

import io.prover.provermvp.camera.Size;
import io.prover.provermvp.gl.TexRect;
import io.prover.provermvp.gl.Texture;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by babay on 05.12.2017.
 */

public class FftHoriz1Program extends GlProgram {
    protected String FRAGMENT_SHADER = "fft_1.frag.glsl";
    protected String VERTEX_SHADER = "copy_scaled.vert.glsl";

    private int texCoordinateLocation;
    private int positionLocation;
    private int textureLocation;
    private int inStepLocation;
    private int widthLocation;
    private int heightLocation;


    @Override
    public void load(Context context) {
        load(context, VERTEX_SHADER, FRAGMENT_SHADER);

        textureLocation = GLES20.glGetUniformLocation(programName, "s_texture");
        inStepLocation = GLES20.glGetUniformLocation(programName, "inStep");
        widthLocation = GLES20.glGetUniformLocation(programName, "width");
        heightLocation = GLES20.glGetUniformLocation(programName, "height");

        texCoordinateLocation = GLES20.glGetAttribLocation(programName, "texCoordinate");
        positionLocation = GLES20.glGetAttribLocation(programName, "position");

    }

    public void bind(Texture sourceTexture, TexRect texRect, Size size) {
        glUseProgram(programName);

        glActiveTexture(GL_TEXTURE0);//GLES20.GL_TEXTURE0
        glBindTexture(GL_TEXTURE_2D, sourceTexture.texId);
        glUniform1i(textureLocation, 0);
        glUniform1f(inStepLocation, 1.0f / size.width);
        glUniform1f(widthLocation, size.width);
        glUniform1f(heightLocation, size.height);

        glEnableVertexAttribArray(texCoordinateLocation);
        glVertexAttribPointer(texCoordinateLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, texRect.textureBuffer);

        glEnableVertexAttribArray(positionLocation);
        glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, texRect.vertexBuffer);
    }

    @Override
    public void unbind() {
        GLES20.glDisableVertexAttribArray(texCoordinateLocation);
        GLES20.glDisableVertexAttribArray(positionLocation);
        GLES20.glUseProgram(0);
    }


}
