package io.prover.provermvp.gl;

import android.opengl.GLES30;

import java.nio.Buffer;

import io.prover.provermvp.camera.Size;

import static android.opengl.GLES20.GL_EXTENSIONS;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LUMINANCE;
import static android.opengl.GLES20.GL_RGB;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glGetString;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES30.GL_R16F;
import static android.opengl.GLES30.GL_R32F;
import static android.opengl.GLES30.GL_RG16F;
import static android.opengl.GLES30.GL_RG32F;
import static android.opengl.GLES30.GL_RGB16F;
import static android.opengl.GLES30.GL_RGB32F;
import static android.opengl.GLES30.GL_RGBA16F;
import static android.opengl.GLES30.GL_RGBA32F;

/**
 * Internal class for storing refs to mTexturesIds for rendering
 */
public class Texture {
    public static final int GL_HALF_FLOAT_OES = 0x8D61;
    public int texId;
    public Size size;

    protected boolean isFloat;
    protected boolean isHalfFloat;

    public Texture(int texId) {
        this.texId = texId;
    }

    public Texture() {
    }

    private static int getFormattGL_EXT_color_buffer_floatInt_32(int colors) {
        switch (colors) {
            case 1:
                return GL_R32F;
            case 2:
                return GL_RG32F;
            case 3:
                return GL_RGB32F;
            case 4:
                return GL_RGBA32F;
        }
        return 0;
    }

    private static int getFormattGL_EXT_color_buffer_floatInt_16(int colors) {
        switch (colors) {
            case 1:
                return GL_R16F;
            case 2:
                return GL_RG16F;
            case 3:
                return GL_RGB16F;
            case 4:
                return GL_RGBA16F;
        }
        return 0;
    }

    private static int getFormattGL_EXT_color_buffer_float(int colors) {
        switch (colors) {
            case 1:
                return GLES30.GL_RED;
            case 2:
                return GLES30.GL_RG;
            case 3:
                return GL_RGB;
            case 4:
                return GL_RGBA;
        }
        return 0;
    }

    private static int getFormat(int colors) {
        switch (colors) {
            case 1:
                return GL_LUMINANCE;
            case 2:
                return GL_RGB;
            case 3:
                return GL_RGB;
            case 4:
                return GL_RGBA;
        }
        return 0;
    }

    public static boolean checkCanDoFloatTexture() {
        String extensions = glGetString(GL_EXTENSIONS);
        if (extensions.contains("GL_EXT_color_buffer_float")) {
            return true;
        } else if (extensions.contains("OES_texture_float")) {
            return true;
        }
        return false;
    }

    public static boolean checkCanDoHalfFloatTexture() {
        String extensions = glGetString(GL_EXTENSIONS);
        if (extensions.contains("OES_texture_half_float")) {
            return true;
        }
        return false;
    }

    public void setTexId(int texId) {
        this.texId = texId;
    }

    @Override
    public String toString() {
        return "[Texture] id: " + texId;
    }

    protected void doTexImage(int colors, Buffer pixels, boolean preferFloat) {
        if (preferFloat) {
            String extensions = glGetString(GL_EXTENSIONS);
            if (extensions.contains("GL_EXT_color_buffer_float")) {
                int formatInt = getFormattGL_EXT_color_buffer_floatInt_32(colors);
                //int formatInt = getFormattGL_EXT_color_buffer_floatInt_16(colors);
                int format = getFormattGL_EXT_color_buffer_float(colors);
                glTexImage2D(GL_TEXTURE_2D, 0, formatInt, size.width, size.height, 0, format, GL_FLOAT, pixels);
                isFloat = true;
            } else if (extensions.contains("OES_texture_float")) {
                int format = getFormat(colors);
                glTexImage2D(GL_TEXTURE_2D, 0, format, size.width, size.height, 0, format, GL_FLOAT, pixels);
                isFloat = true;
            } else if (extensions.contains("OES_texture_half_float")) {
                int format = getFormat(colors);
                glTexImage2D(GL_TEXTURE_2D, 0, format, size.width, size.height, 0, format, GL_HALF_FLOAT_OES, pixels);
                isHalfFloat = true;
            }
        } else {
            int format = getFormat(colors);
            glTexImage2D(GL_TEXTURE_2D, 0, format, size.width, size.height, 0, format, GL_UNSIGNED_BYTE, pixels);
        }
    }

    public boolean isFloat() {
        return isFloat;
    }

    public boolean isHalfFloat() {
        return isHalfFloat;
    }

}
