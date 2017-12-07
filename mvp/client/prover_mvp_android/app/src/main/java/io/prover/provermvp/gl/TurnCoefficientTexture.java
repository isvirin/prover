package io.prover.provermvp.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import io.prover.provermvp.camera.Size;

import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glTexParameteri;

/**
 * Created by babay on 06.12.2017.
 */

public class TurnCoefficientTexture extends Texture {

    //public float[][] values;
    public int actualRows;

    public static int log2nlz(int bits) {
        if (bits == 0)
            return 0; // or throw exception
        return 31 - Integer.numberOfLeadingZeros(bits);
    }

    /**
     * @param texId
     * @param size
     * @param neg   arg = 2 * PI * k / N * (neg ? -1 : 1); true for FFT, false for BFFT
     */
    public void init(int texId, int size, boolean neg) {
        setTexId(texId);
        glBindTexture(GL_TEXTURE_2D, texId);

        float[][] values = generateCoefficients(size, neg);
        this.size = new Size(size, values.length);
        ByteBuffer buffer = ByteBuffer.allocateDirect(values.length * values[0].length * 4).order(ByteOrder.nativeOrder());
        FloatBuffer bufferFloat = buffer.asFloatBuffer();
        bufferFloat.position(0);
        for (float[] row : values) {
            bufferFloat.put(row);
        }
        bufferFloat.position(0);
        doTexImage(2, bufferFloat);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private float[][] generateCoefficients(int size, boolean neg) {
        double base = 2 * Math.PI * (neg ? -1 : 1);
        int rows = log2nlz(size) - 1;
        actualRows = rows;
        int potrows = 2 << log2nlz(rows);
        if (potrows < rows)
            potrows = potrows << 1;
        float[][] result = new float[potrows][size * 2];
        for (int r = 0; r < rows; r++) {
            int N = 4 << r;
            double _2_pi_N = base / N;
            float[] row = result[r];
            for (int k = 0; k < size; k++) {
                double arg = _2_pi_N * k;
                int pos = 2 * k;
                float val = (float) Math.cos(arg);
                row[pos] = (val > -1e-8 && val < 1e-8) ? 0 : val;
                val = (float) Math.sin(arg);
                row[pos + 1] = (val > -1e-8 && val < 1e-8) ? 0 : val;
            }
        }
        return result;
    }
}
