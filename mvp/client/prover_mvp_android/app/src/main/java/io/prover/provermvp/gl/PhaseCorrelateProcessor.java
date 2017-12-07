package io.prover.provermvp.gl;

import android.content.Context;
import android.util.Log;

import io.prover.provermvp.camera.Size;
import io.prover.provermvp.gl.prog.FftHoriz1Program;
import io.prover.provermvp.gl.prog.FftHorizProgram;
import io.prover.provermvp.gl.prog.ReadCameraToGrayscaleProgram;
import io.prover.provermvp.gl.prog.ReversePosCopyProgram;

/**
 * Created by babay on 01.12.2017.
 */

public class PhaseCorrelateProcessor {
    private static final int SIZE = 1024;
    private final ReadCameraToGrayscaleProgram readCameraProgram = new ReadCameraToGrayscaleProgram();
    private final ReversePosCopyProgram reversePosCopyProgram = new ReversePosCopyProgram();
    private final FftHoriz1Program fftHoriz1Program = new FftHoriz1Program();
    private final FftHorizProgram fftHorizProgram = new FftHorizProgram();
    private final int[] revOrderTable = new int[256];
    //TextureFBO imageReceiverFBO;
    //TextureFBO reIndexedImage;
    TextureFBO fftFbo1;
    TextureFBO fftFbo2;
    TextureFBO fftFboResult;
    //private final TurnCoefficientTexture turnCoeffPrecalcTexture = new TurnCoefficientTexture();


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

    public static short obviousReverse(short inShort, int unusedBits) {
        int in = inShort & 0xffff;
        int out = in; // r will be reversed bits of v; first get LSB of v
        int s = 16 - 1; // extra shift needed at end

        for (in >>= 1; in != 0; in >>= 1) {
            out <<= 1;
            out |= in & 1;
            s--;
        }
        out <<= s; // shift when v's highest bits are zero
        return (short) ((out & 0xffff) >> unusedBits);
    }

    public boolean isInitialised() {
        return fftFbo1 != null;
    }

    public void init(Context context, GlTextures textures) {
        readCameraProgram.load(context);
        reversePosCopyProgram.load(context);
        fftHoriz1Program.load(context);
        fftHorizProgram.load(context);
        //imageReceiverFBO = new TextureFBO(textures.getNextTexture(), new Size(1024, 1024), 1);
        //reIndexedImage = new TextureFBO(textures.getNextTexture(), new Size(1024, 1024), 1);
        fftFbo1 = new TextureFBO(textures.getNextTexture(), new Size(SIZE, SIZE), 4);
        fftFbo2 = new TextureFBO(textures.getNextTexture(), new Size(SIZE, SIZE), 4);
        //turnCoeffPrecalcTexture.init(textures.getNextTexture(), SIZE, true);
    }

    public void release() {
        /*imageReceiverFBO.release();
        imageReceiverFBO = null;
        reIndexedImage.release();
        reIndexedImage = null;*/
        fftFbo1.release();
        fftFbo1 = null;
        fftFbo2.release();
        fftFbo2 = null;
    }

    public void draw(CameraTexture cam, TexRect texRect) {
        long start = System.currentTimeMillis();
        fftFbo1.bindAsTarget();
        readCameraProgram.bind(cam.texId, cam.mCameraTransformMatrix, texRect, revOrderTable);
        texRect.draw();
        readCameraProgram.unbind();

        fftFbo2.bindAsTarget();
        reversePosCopyProgram.bind(fftFbo1, texRect, SIZE, SIZE, revOrderTable);
        texRect.draw();
        reversePosCopyProgram.unbind();

        fftFbo1.bindAsTarget();
        fftHoriz1Program.bind(fftFbo2, texRect, fftFbo2.size);
        texRect.draw();
        fftHoriz1Program.unbind();

        //int maxStep = turnCoeffPrecalcTexture.actualRows;
        int maxStep = 10;

        fftHorizProgram.bind(texRect, fftFbo1.size);

        for (int i = 1; i <= maxStep; i++) {
            TextureFBO source = i % 2 == 1 ? fftFbo1 : fftFbo2;
            fftFboResult = i % 2 == 1 ? fftFbo2 : fftFbo1;

            fftFboResult.bindAsTarget();
            fftHorizProgram.updateBinding(source, source.size, i);

            texRect.draw();
        }
        fftHorizProgram.unbind();

        /*fftFbo2.bindAsTarget();
        fftHorizProgram.bind(fftFbo1, turnCoeffPrecalcTexture, texRect, fftFbo1.size, 1);
        texRect.draw();
        fftHorizProgram.unbind();
        fftFboResult = fftFbo2;*/

        fftFboResult.unbindAsTarget();
        long end = System.currentTimeMillis();
        Log.d("openGL_calc", "it took" + (end - start));
    }

    public TextureFBO getResultFbo() {
        return fftFboResult;
    }
}
