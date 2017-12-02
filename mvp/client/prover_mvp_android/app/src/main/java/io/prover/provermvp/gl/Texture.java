package io.prover.provermvp.gl;

/**
 * Internal class for storing refs to mTexturesIds for rendering
 */
public class Texture {
    public int texId;

    public Texture(int texId) {
        this.texId = texId;
    }

    public Texture() {
    }

    public void setTexId(int texId) {
        this.texId = texId;
    }

    @Override
    public String toString() {
        return "[Texture] id: " + texId;
    }
}
