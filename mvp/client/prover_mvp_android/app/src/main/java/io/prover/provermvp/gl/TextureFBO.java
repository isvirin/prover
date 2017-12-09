package io.prover.provermvp.gl;

import android.opengl.GLES20;

import java.nio.Buffer;

import io.prover.provermvp.camera.Size;
import io.prover.provermvp.gl.lib.GlUtil;

import static android.opengl.GLES20.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteFramebuffers;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glReadPixels;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexParameteri;

/**
 * Created by babay on 01.12.2017.
 */

public class TextureFBO extends Texture {
    private final int fboNames[] = new int[1];
    private int name;

    public TextureFBO(int texId, Size size, int planes) {
        super(texId);
        this.size = size;
        glGenFramebuffers(1, fboNames, 0);
        name = fboNames[0];

        glBindFramebuffer(GL_FRAMEBUFFER, fboNames[0]);
        glBindTexture(GL_TEXTURE_2D, texId);

        doTexImage(planes, null, true);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        GLES20.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texId, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GlUtil.checkGlError2("TextureFBO generate");
    }

    public TextureFBO(int texId, Size size, int formatInt, int format, int type) {
        super(texId);
        this.size = size;
        glGenFramebuffers(1, fboNames, 0);
        name = fboNames[0];

        glBindFramebuffer(GL_FRAMEBUFFER, fboNames[0]);
        glBindTexture(GL_TEXTURE_2D, texId);

        glTexImage2D(GL_TEXTURE_2D, 0, formatInt, size.width, size.height, 0, format, type, null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        GLES20.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texId, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GlUtil.checkGlError2("TextureFBO generate");
    }


    public void release() {
        if (name != 0) {
            glDeleteFramebuffers(fboNames.length, fboNames, 0);
            name = 0;
        }
    }

    public void read(Buffer target, int format) {
        target.position(0);
        glBindFramebuffer(GL_FRAMEBUFFER, fboNames[0]);
        int valueFormat = GL_UNSIGNED_BYTE;
        if (isFloat)
            valueFormat = GL_FLOAT;
        else if (isHalfFloat)
            valueFormat = GL_HALF_FLOAT_OES;
        glReadPixels(0, 0, size.width, size.height, format, valueFormat, target);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public void read(Buffer target, int format, int type) {
        target.position(0);
        glBindFramebuffer(GL_FRAMEBUFFER, fboNames[0]);
        glReadPixels(0, 0, size.width, size.height, format, type, target);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public void bindAsTarget() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, name);
        GLES20.glViewport(0, 0, size.width, size.height);
    }

    public void unbindAsTarget() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

}
