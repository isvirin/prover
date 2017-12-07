package io.prover.provermvp.gl.prog;

import android.content.Context;
import android.opengl.GLES20;

import io.prover.provermvp.gl.lib.GlUtil;

/**
 * Created by babay on 17.10.2015.
 */
public abstract class GlProgram {
    public int programName;

    public GlProgram() {
    }

    protected void load(Context context, String vertexShaderFileName, String fragShaderFileName) {
        programName = ESShader.loadProgramFromAsset(context, vertexShaderFileName, fragShaderFileName);
        GlUtil.checkGlError2("load program" + fragShaderFileName);
        //Log.d("GlProgram", String.format("Loaded gl program %d as %s", programName, getClass().getSimpleName()));
    }

    public void release() {
        if (programName == 0) {
            GLES20.glDeleteProgram(programName);
            //Log.d("GlProgram", String.format("Deleted gl program%d (was loaded as %s)", programName, getClass().getSimpleName()));
            programName = 0;
        }
    }

    public abstract void unbind();

    public abstract void load(Context context);

    public boolean isReleased() {
        return programName == 0;
    }

    public void assertAllocated() {
        if (programName == 0) {
            throw new RuntimeException("program is 0");
        }
    }

}
