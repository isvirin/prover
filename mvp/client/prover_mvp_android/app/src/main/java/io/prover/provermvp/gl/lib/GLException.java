package io.prover.provermvp.gl.lib;

import android.opengl.GLU;

/**
 * Created by babay on 19.10.2015.
 */
public class GLException extends Exception {
    private final int mError;

    public GLException(final int error) {
        super(getErrorString(error));
        mError = error;
    }

    public GLException(final int error, final String string) {
        super(string + ": " + getErrorString(error));
        mError = error;
    }

    private static String getErrorString(int error) {
        String errorString = GLU.gluErrorString(error);
        if (errorString == null) {
            errorString = "Unknown error 0x" + Integer.toHexString(error);
        }
        return errorString;
    }

    int getError() {
        return mError;
    }
}
