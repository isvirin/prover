package io.prover.provermvp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.ethereum.crypto.ECKey;
import org.spongycastle.jcajce.provider.asymmetric.ec.EtherKeccakOutputStream;
import org.spongycastle.jcajce.provider.asymmetric.ec.EtherKeccakSigner;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;

import static io.prover.provermvp.Const.TAG;

/**
 * Created by babay on 12.11.2017.
 */

public class Etherium {

    private static final String KEY_PRIVATE = "key";

    private static volatile Etherium instance;

    static {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        provider.addAlgorithm("Signature.EtherKeccakRecoverable", EtherKeccakSigner.class.getCanonicalName());
        Security.insertProviderAt(provider, 1);
    }

    ECKey keyPair;

    public Etherium(Context context) {
        keyPair = loadKey(context);
        if (keyPair == null) {
            try {
                keyPair = generateKeyPair();
                saveKey(context);
            } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
                e.printStackTrace();
            }
        }
    }

    public static Etherium getInstance(Context context) {
        Etherium local = instance;
        if (local == null) {
            synchronized (Etherium.class) {
                local = instance;
                if (local == null) {
                    instance = local = new Etherium(context);
                }
            }
        }
        return local;
    }

    public byte[] sign(byte[] data) {
        try {
            EtherKeccakOutputStream signerStream = new EtherKeccakOutputStream();
            signerStream.initSign(keyPair.getPrivate(), new SecureRandom());
            signerStream.write(data);
            return signerStream.getSignature();
        } catch (InvalidKeyException | IOException | SignatureException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return null;
    }

    public boolean verify(byte[] data) {
        try {
            EtherKeccakOutputStream verifierStream = new EtherKeccakOutputStream();
            verifierStream.initVerify(keyPair.getPublic());
            verifierStream.write(data);
            return verifierStream.verify(data);
        } catch (InvalidKeyException | SignatureException | IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return false;
    }

    public ECKey generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        return new ECKey(new SecureRandom());
        /*ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDsA", "SC");
        keyGen.initialize(ecSpec, new SecureRandom());
        return keyGen.generateKeyPair();*/
    }

    private void saveKey(Context context) {
        if (keyPair != null) {
            BigInteger d = keyPair.getPrivKey();
            String key = new String(Base64.encodeBase64(d.toByteArray()));
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_PRIVATE, key).apply();
        }
    }

    private ECKey loadKey(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.contains(KEY_PRIVATE))
            return null;

        String value = prefs.getString(KEY_PRIVATE, null);
        byte[] bytes = Base64.decode(value.toCharArray());

        return ECKey.fromPrivate(bytes);
    }
}
