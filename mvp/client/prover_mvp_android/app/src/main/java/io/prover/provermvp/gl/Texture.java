package io.prover.provermvp.gl;

/**
 * Internal class for storing refs to mTexturesIds for rendering
 */
class Texture {
    public int texNum;
    public int texId;
    public String uniformName;

    Texture(int texNum, int texId, String uniformName) {
        this.texNum = texNum;
        this.texId = texId;
        this.uniformName = uniformName;
    }

    @Override
    public String toString() {
        return "[Texture] num: " + texNum + " id: " + texId + ", uniformName: " + uniformName;
    }

}
