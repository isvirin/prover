package io.prover.provermvp.detector;

import android.graphics.Matrix;

/**
 * Created by babay on 27.11.2017.
 */

public class SwypeOrientationHelper {

    public static String rotateSwypeCode(String code, int orientationHint) {
        if (orientationHint == 0)
            return code;
        char[] chars = code.toCharArray();
        float[] pt = new float[2];
        Matrix m = getSwypeMatrix(orientationHint);
        for (int i = 0; i < chars.length; i++) {
            char aChar = chars[i];
            int pos = aChar - '1';
            pt[0] = pos % 3;
            pt[1] = pos / 3;
            m.mapPoints(pt);
            pos = Math.round(pt[1] * 3 + pt[0]);

            chars[i] = (char) ('1' + pos);
        }
        return new String(chars);
    }

    public static Matrix getSwypeMatrix(int orientationHint) {
        Matrix matrix = new Matrix();
        if (orientationHint == 0)
            return matrix;

        matrix.postRotate(orientationHint, 1, 1);
        return matrix;
    }

    /**
     * @param position,       zero-based
     * @param orientationhint
     * @return
     */
    private static int rotateSqypePosition(int position, int orientationhint) {
        /*if (position == 4)
            return 4;
        if (orientationhint == 90){
            switch (position){
                case 0:
                    return 2;
                case 1:
                    return 5;
                case 2:
                    return 8;
                case 3:
                    return 1;
                case 5:
                    return 7;
                case 6:
                    return 0;
                case 7:
                    return 3;
                case 8:
                    return 6;
            }
        }*/
        return position;
    }
}
