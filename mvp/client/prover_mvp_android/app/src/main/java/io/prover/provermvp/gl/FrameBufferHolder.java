package io.prover.provermvp.gl;

import android.opengl.GLES20;

import io.prover.provermvp.camera.Size;

import static android.opengl.GLES20.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_RGB;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteFramebuffers;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexParameteri;

/**
 * Created by babay on 24.11.2017.
 */

public class FrameBufferHolder {

    public int[] fboNames;
    public int[] textureNames;
    public Size size;

    public void init(int amount, Size size) {
        this.size = size;
        fboNames = new int[amount];
        textureNames = new int[amount];

        glGenFramebuffers(amount, fboNames, 0);
        glGenTextures(amount, textureNames, 0);
        //ByteBuffer buf = ByteBuffer.allocateDirect(size.width * size.height * 4 * 4);

        for (int i = 0; i < amount; i++) {
            glBindFramebuffer(GL_FRAMEBUFFER, fboNames[i]);
            glBindTexture(GL_TEXTURE_2D, textureNames[i]);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, size.width, size.height, 0, GL_RGB, GL_UNSIGNED_BYTE, null);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

            GLES20.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureNames[i], 0);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }
    }

    public void deinit() {
        if (fboNames != null) {
            glDeleteFramebuffers(fboNames.length, fboNames, 0);
        }
        if (textureNames != null) {
            glDeleteTextures(textureNames.length, textureNames, 0);
        }
        fboNames = null;
        textureNames = null;
    }

    boolean isInitialised() {
        return fboNames != null;
    }
}
