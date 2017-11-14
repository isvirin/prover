package org.spongycastle.jcajce.provider.asymmetric.ec;

import org.spongycastle.operator.OperatorStreamException;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

public class EtherKeccakOutputStream extends OutputStream {

    private final Signature sig;

    public EtherKeccakOutputStream() {
        try {
            sig = Signature.getInstance("EtherKeccakRecoverable", "SC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public void initSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
        sig.initSign(privateKey, random);
    }

    public void initVerify(PublicKey publicKey) throws InvalidKeyException {
        sig.initVerify(publicKey);
    }

    public void write(byte[] bytes, int off, int len) throws IOException {
        try {
            sig.update(bytes, off, len);
        } catch (SignatureException e) {
            throw new OperatorStreamException("exception in content signer: " + e.getMessage(), e);
        }
    }

    public void write(byte[] bytes) throws IOException {
        try {
            sig.update(bytes);
        } catch (SignatureException e) {
            throw new OperatorStreamException("exception in content signer: " + e.getMessage(), e);
        }
    }

    public void write(int b) throws IOException {
        try {
            sig.update((byte) b);
        } catch (SignatureException e) {
            throw new OperatorStreamException("exception in content signer: " + e.getMessage(), e);
        }
    }

    public byte[] getSignature() throws SignatureException {
        return sig.sign();
    }

    public boolean verify(byte[] sigBytes) throws SignatureException {
        return sig.verify(sigBytes);
    }
}