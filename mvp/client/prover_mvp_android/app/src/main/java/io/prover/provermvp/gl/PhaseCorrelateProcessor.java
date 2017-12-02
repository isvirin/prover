package io.prover.provermvp.gl;

import android.content.Context;
import android.opengl.GLES20;

import io.prover.provermvp.camera.Size;
import io.prover.provermvp.gl.prog.ReadCameraToGrayscaleProgram;
import io.prover.provermvp.gl.prog.ReversePosCopyProgram;

import static android.opengl.GLES20.GL_RGBA;

/**
 * Created by babay on 01.12.2017.
 */

public class PhaseCorrelateProcessor {
    private final ReadCameraToGrayscaleProgram readCameraProgram = new ReadCameraToGrayscaleProgram();
    private final ReversePosCopyProgram reversePosCopyProgram = new ReversePosCopyProgram();
    private final int[] revOrderTable = new int[256];
    TextureFBO imageReceiverFBO;
    TextureFBO reIndexedImage;
    TextureFBO fftFbo1;
    TextureFBO fftFbo2;
    TextureFBO fftFboResult;


    public PhaseCorrelateProcessor() {
        for (int i = 0; i < revOrderTable.length; i++) {
            byte rev = (byte) ((i * 0x0202020202L & 0x010884422010L) % 1023);
            revOrderTable[i] = rev & 0xff;
        }
    }

    public static int obviousReverse(byte inByte) {
        int in = inByte & 0xff;
        int out = in; // r will be reversed bits of v; first get LSB of v
        int s = 8 - 1; // extra shift needed at end

        for (in >>= 1; in != 0; in >>= 1) {
            out <<= 1;
            out |= in & 1;
            s--;
        }
        out <<= s; // shift when v's highest bits are zero
        return out & 0xff;
    }

    public static int obviousReverse(short inShort) {
        int in = inShort & 0xffff;
        int out = in; // r will be reversed bits of v; first get LSB of v
        int s = 16 - 1; // extra shift needed at end

        for (in >>= 1; in != 0; in >>= 1) {
            out <<= 1;
            out |= in & 1;
            s--;
        }
        out <<= s; // shift when v's highest bits are zero
        return out & 0xffff;
    }

    public boolean isInitialised() {
        return imageReceiverFBO != null;
    }

    public void init(Context context, GlTextures textures) {
        readCameraProgram.load(context);
        reversePosCopyProgram.load(context);
        imageReceiverFBO = new TextureFBO(textures.getNextTexture(), new Size(1024, 1024), GL_RGBA);
        reIndexedImage = new TextureFBO(textures.getNextTexture(), new Size(1024, 1024), GL_RGBA);
        fftFbo1 = new TextureFBO(textures.getNextTexture(), new Size(512, 1024), GL_RGBA);
        fftFbo2 = new TextureFBO(textures.getNextTexture(), new Size(512, 1024), GL_RGBA);
    }

    public void release() {
        imageReceiverFBO.release();
        imageReceiverFBO = null;
        reIndexedImage.release();
        reIndexedImage = null;
    }

    public void draw(CameraTexture cam, TexRect texRect) {
        readCameraProgram.bind(cam.texId, cam.mCameraTransformMatrix, texRect.textureBuffer, texRect.vertexBuffer, revOrderTable);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, imageReceiverFBO.fboNames[0]);
        GLES20.glViewport(0, 0, imageReceiverFBO.size.width, imageReceiverFBO.size.height);

        texRect.draw();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        readCameraProgram.unbind();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, reIndexedImage.fboNames[0]);
        reversePosCopyProgram.bind(imageReceiverFBO, texRect, revOrderTable);
        texRect.draw();
        reversePosCopyProgram.unbind();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public TextureFBO getResultFbo() {
        return reIndexedImage;
    }
}
