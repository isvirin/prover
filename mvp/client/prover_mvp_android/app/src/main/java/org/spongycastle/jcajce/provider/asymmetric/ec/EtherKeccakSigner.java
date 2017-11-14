package org.spongycastle.jcajce.provider.asymmetric.ec;

import org.spongycastle.crypto.digests.KeccakDigest;

import java.math.BigInteger;
import java.security.SignatureException;

public class EtherKeccakSigner extends SignatureSpi {
    private final RecoverableDSAEncoder encoder;
    private final ECDSARecoverableSigner signer;

    public EtherKeccakSigner() {
        super(new KeccakDigest(256), new ECDSARecoverableSigner(), new RecoverableDSAEncoder());
        encoder = (RecoverableDSAEncoder) super.encoder;
        signer = (ECDSARecoverableSigner) super.signer;
    }

    public byte[] engineSign()
            throws SignatureException {
        byte[] hash = new byte[digest.getDigestSize()];

        digest.doFinal(hash, 0);

        try {
            BigInteger[] sig = signer.generateSignature(hash);
            if (sig.length == 3)
                return encoder.encodeRecoverable(sig[0], sig[1], sig[2]);

            return encoder.encode(sig[0], sig[1]);
        } catch (Exception e) {
            throw new SignatureException(e);
        }
    }
}