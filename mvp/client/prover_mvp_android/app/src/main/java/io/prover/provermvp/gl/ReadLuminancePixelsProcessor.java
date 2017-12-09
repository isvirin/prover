package io.prover.provermvp.gl;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;

import io.prover.provermvp.camera.Size;
import io.prover.provermvp.gl.prog.ReadCameraToGrayscalePackProgram;

import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;

/**
 * Created by babay on 08.12.2017.
 */

public class ReadLuminancePixelsProcessor {
    private final ReadCameraToGrayscalePackProgram readCameraProgram = new ReadCameraToGrayscalePackProgram();
    ByteBuffer buffer;
    TextureFBO fbo;

    public void init(Context context, GlTextures textures, Size size) {
        readCameraProgram.load(context);
        Size fboSize = new Size(size.width / 4, size.height);
        fbo = new TextureFBO(textures.getNextTexture(), fboSize, GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE);
        buffer = ByteBuffer.allocateDirect(size.width * size.height * 4);
    }

    public void draw(CameraTexture cam, TexRect texRect) {
        long start = System.currentTimeMillis();

        fbo.bindAsTarget();
        readCameraProgram.bind(cam.texId, cam.mCameraTransformMatrix, cam.mScreenTransformMatrix, texRect, fbo.size.width);
        texRect.draw();
        readCameraProgram.unbind();
        fbo.unbindAsTarget();
        fbo.read(buffer, GL_RGBA, GL_UNSIGNED_BYTE);
        buffer.position(0);

        long end = System.currentTimeMillis();
        Log.d("OpenGL", "glReadPixels: " + (end - start));

    }

    public void release() {
        fbo.release();
        fbo = null;
    }

    public boolean isInitialised() {
        return fbo != null;
    }
}
