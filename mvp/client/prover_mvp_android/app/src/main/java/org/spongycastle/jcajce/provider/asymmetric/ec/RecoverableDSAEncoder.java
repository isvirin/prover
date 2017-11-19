package org.spongycastle.jcajce.provider.asymmetric.ec;

import org.spongycastle.jcajce.provider.asymmetric.util.DSAEncoder;

import java.io.IOException;
import java.math.BigInteger;

public class RecoverableDSAEncoder
        implements DSAEncoder {
    public byte[] encode(
            BigInteger r,
            BigInteger s)
            throws IOException {
        byte[] first = makeUnsigned(r);
        byte[] second = makeUnsigned(s);
        byte[] res;

        if (first.length > second.length) {
            res = new byte[first.length * 2];
        } else {
            res = new byte[second.length * 2];
        }

        System.arraycopy(first, 0, res, res.length / 2 - first.length, first.length);
        System.arraycopy(second, 0, res, res.length - second.length, second.length);

        return res;
    }

    public byte[] encodeRecoverable(BigInteger r, BigInteger s, BigInteger v) throws IOException {
        byte[] first = makeUnsigned(r);
        byte[] second = makeUnsigned(s);
        byte[] third = v.toByteArray();

        byte[] res = new byte[65];

        System.arraycopy(first, 0, res, 32 - first.length, first.length);
        System.arraycopy(second, 0, res, 64 - second.length, second.length);
        System.arraycopy(third, 0, res, 64, 1);

        return res;
    }


    private byte[] makeUnsigned(BigInteger val) {
        byte[] res = val.toByteArray();

        if (res[0] == 0) {
            byte[] tmp = new byte[res.length - 1];
            System.arraycopy(res, 1, tmp, 0, tmp.length);
            return tmp;
        }

        return res;
    }

    public BigInteger[] decode(byte[] encoding) throws IOException {

        BigInteger[] sig = new BigInteger[3];

        byte[] first = new byte[32];
        byte[] second = new byte[32];
        byte[] third = new byte[1];

        System.arraycopy(encoding, 0, first, 0, first.length);
        System.arraycopy(encoding, first.length, second, 0, second.length);
        System.arraycopy(encoding, 64, third, 0, 1);

        sig[0] = new BigInteger(1, first);
        sig[1] = new BigInteger(1, second);
        sig[2] = new BigInteger(1, third);

        return sig;
    }
}