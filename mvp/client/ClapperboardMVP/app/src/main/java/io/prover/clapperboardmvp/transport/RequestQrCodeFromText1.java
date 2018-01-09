package io.prover.clapperboardmvp.transport;

import org.ethereum.core.Transaction;
import org.json.JSONException;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.BigIntegers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import io.prover.clapperboardmvp.transport.responce.HashResponce;

/**
 * Created by babay on 22.12.2017.
 */

public class RequestQrCodeFromText1 extends TransactionNetworkRequest<HashResponce> {

    public static final String METHOD = "submit-message";
    protected static final int GAS_LIMIT = 2_000_000;
    private static final int GENERATE_QRCODE_METHOD = 0x708b34fe;
    private final String message;

    public RequestQrCodeFromText1(NetworkSession session, String message, NetworkRequestListener listener) {
        super(session, METHOD, listener);
        this.message = message;
    }

    @Override
    protected HashResponce parse(String source) throws IOException, JSONException {
        return new HashResponce(source);
    }

    @Override
    protected Transaction createTransaction() {
        byte[] stringBytes;
        try {
            stringBytes = message.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        int len = stringBytes.length;
        int param1ValueLength = (len / 32 + (len % 32 == 0 ? 0 : 1)) * 32;

        byte[] nonce = BigIntegers.asUnsignedByteArray(session.getNonce());
        byte[] method = BigIntegers.asUnsignedByteArray(BigInteger.valueOf(GENERATE_QRCODE_METHOD));
        byte[] param1Head = BigIntegers.asUnsignedByteArray(32, BigInteger.valueOf(32));
        byte[] param1StrLength = BigIntegers.asUnsignedByteArray(32, BigInteger.valueOf(stringBytes.length));
        byte[] param1Value = new byte[param1ValueLength];
        if (stringBytes.length > 0)
            System.arraycopy(stringBytes, 0, param1Value, 0, stringBytes.length);
        byte[] data = Arrays.concatenate(method, param1Head, param1StrLength, param1Value);

        BigInteger price = session.getGasPriceBigInt().multiply(BigInteger.valueOf(3)).divide(BigInteger.valueOf(2));
        byte[] gasPrice = BigIntegers.asUnsignedByteArray(price);

        BigInteger availGas = session.getMaxGasLimit(price);
        int estimateGas = (data.length - 68) * 80 + 23715;
        BigInteger maxGas = BigInteger.valueOf(estimateGas * 3 / 2);
        if (availGas.compareTo(maxGas) < 0)
            maxGas = availGas;
        byte[] gasLimit = BigIntegers.asUnsignedByteArray(maxGas);

        Transaction transaction = new Transaction(nonce, gasPrice, gasLimit, session.getContractAddress(), new byte[]{0}, data);
        transaction.sign(session.key);
        return transaction;
    }
}
