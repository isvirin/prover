package io.prover.provermvp.gl;

import io.prover.provermvp.gl.lib.GlUtil;

import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;

/**
 * Created by babay on 01.12.2017.
 */

public class GlTextures {
    private final int MAX_TEXTURES = 16;
    private final int textureNames[] = new int[MAX_TEXTURES];
    int nextUnusedTexture = 0;
    private boolean initialised = false;

    public GlTextures() {
    }

    public void init() {
        glGenTextures(MAX_TEXTURES, textureNames, 0);
        GlUtil.checkGlError2("Texture generate");
        initialised = true;
    }

    public void release() {
        if (initialised) {
            glDeleteTextures(MAX_TEXTURES, textureNames, 0);
            initialised = false;
        }
    }

    public int getNextTexture() {
        return textureNames[nextUnusedTexture++];
    }

}
