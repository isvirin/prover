package io.prover.provermvp.transport;

import android.util.Log;

import org.ethereum.core.Transaction;
import org.json.JSONException;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.DecoderException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import io.prover.provermvp.transport.responce.FixableEtheriumExcetion;
import io.prover.provermvp.transport.responce.SubmitVideoHashResponce;
import io.prover.provermvp.transport.responce.SwypeResponce1;

/**
 * Created by babay on 15.11.2017.
 */

public class SubmitVideoHashRequest extends NetworkRequest<SubmitVideoHashResponce> {

    private static final int GAS_LIMIT = 1_000_000;
    private static final int SUDMIT_VIDEO_FILE_DATA = 0xa0ee3ecf;
    private static final String METHOD = "submit-media-hash";

    private final NetworkSession session;
    private final SwypeResponce1 responce1;
    private final File videoFile;

    private byte[] videoFileHash;

    public SubmitVideoHashRequest(NetworkSession session, SwypeResponce1 responce1, File videoFile, NetworkRequestListener listener) {
        super(listener);
        this.session = session;
        this.responce1 = responce1;
        this.videoFile = videoFile;
    }

    @Override
    public void run() {
        listener.onNetworkRequestStart(this);
        try {
            if (videoFileHash == null) {
                videoFileHash = calculateFileHash();
            }
            try {
                SubmitVideoHashResponce responce = postTransaction(METHOD, "hex=0x", null);
                session.increaseNonce();
                if (!cancelled) {
                    listener.onNetworkRequestDone(this, responce);
                }
            } catch (FixableEtheriumExcetion e) {
                if (debugData != null) {
                    debugData.setException(e).log();
                }
                session.increaseNonce();
                execute();
            } catch (IOException | DecoderException ex) {
                handleException(ex);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    protected SubmitVideoHashResponce parse(String source) throws IOException, JSONException {
        return new SubmitVideoHashResponce(source);
    }

    @Override
    protected Transaction createTransaction() {
        Log.d(TAG, "SubmitVideoHashRequest nonce: " + session.getNonce().toString());
        byte[] gasLimit = toUnsignedByteArray(BigInteger.valueOf(GAS_LIMIT));
        byte[] operation = toUnsignedByteArray(BigInteger.valueOf(SUDMIT_VIDEO_FILE_DATA));
        byte[] data = Arrays.concatenate(operation, videoFileHash, responce1.hashBytes);
        byte[] nonce = toUnsignedByteArray(session.getNonce());
        Transaction transaction = new Transaction(nonce, session.getGasPrice(), gasLimit, session.getContractAddress(), new byte[]{0}, data);
        transaction.sign(session.key);
        return transaction;
    }

    private byte[] calculateFileHash() throws NoSuchProviderException, NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("sha256", "SC");

        byte[] buf = new byte[4096];

        FileInputStream stream = new FileInputStream(videoFile);
        while (stream.available() > 0) {
            int amount = stream.read(buf);
            digest.update(buf, 0, amount);
        }

        return digest.digest();
    }

}
