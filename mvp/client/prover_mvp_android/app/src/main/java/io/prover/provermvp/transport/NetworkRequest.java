package io.prover.provermvp.transport;

import android.os.AsyncTask;
import android.util.Log;

import org.ethereum.core.Transaction;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;

import io.prover.provermvp.BuildConfig;
import io.prover.provermvp.Const;
import io.prover.provermvp.transport.responce.KnownTransactionException;
import io.prover.provermvp.transport.responce.LowFundsException;
import io.prover.provermvp.transport.responce.NonceTooLowException;

/**
 * Created by babay on 14.11.2017.
 */

public abstract class NetworkRequest<T> implements Runnable {
    public static final int CONNECT_TIMEOUT = 10_000;
    public static final int READ_TIMEOUT = 30_000;
    static final String TAG = Const.TAG + "NetRequest";
    protected final NetworkRequestListener listener;
    protected RequestLog debugData;
    protected String charset = "UTF-8";
    volatile boolean cancelled;

    public NetworkRequest(NetworkRequestListener listener) {
        this.listener = listener;
    }

    public void execute() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(this);
    }

    protected HttpURLConnection createConnection(String method, RequestType requestType, boolean doOutput) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) ((new URL(Const.SERVER_URL + method).openConnection()));
        urlConnection.setReadTimeout(READ_TIMEOUT);
        urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
        urlConnection.setRequestMethod(requestType.requestTypeString());

        if (doOutput) {
            urlConnection.setDoOutput(true);
            if (!urlConnection.getRequestProperties().containsKey("Content-Type")) {
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            }
        }
        urlConnection.setRequestProperty("Accept", "application/json");

        if (BuildConfig.DEBUG) {
            debugData = new RequestLog(requestType);
            debugData.onStart(urlConnection);
        }

        return urlConnection;
    }

    protected void writeRequestBody(HttpURLConnection urlConnection, String requestBody) throws IOException {
        if (debugData != null)
            debugData.requestBody = requestBody;

        try (OutputStream outputStream = urlConnection.getOutputStream();
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset))) {
            writer.write(requestBody);
            writer.flush();
        }
    }

    protected String readResponce(HttpURLConnection urlConnection) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), charset))) {
            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            String result = sb.toString();
            if (debugData != null)
                debugData.onGotResponse(result);
            return result;
        }
    }

    protected String postEnclosingRequest(String method, RequestType requestType, String requestBody) throws IOException, JSONException {
        HttpURLConnection urlConnection = createConnection(method, requestType, requestBody != null);
        if (debugData != null) {
            Log.d(TAG, "sending " + debugData.toString());
        }
        urlConnection.connect();
        if (requestBody != null) {
            writeRequestBody(urlConnection, requestBody);
        }
        String responce = readResponce(urlConnection);
        if (debugData != null)
            Log.d(TAG, debugData.toString());
        return responce;
    }

    protected void execSimpleRequest(String method, RequestType requestType, String requestBody) {
        try {
            String responceStr = postEnclosingRequest(method, requestType, requestBody);
            T responce = parse(responceStr);
            if (!cancelled) {
                listener.onNetworkRequestDone(this, responce);
            }
        } catch (IOException | JSONException ex) {
            handleException(ex);
        }
    }

    protected T postTransaction(String method, String prefix, Transaction transaction) throws IOException, JSONException {
        if (transaction == null)
            transaction = createTransaction();
        byte[] bytes = transaction.getEncoded();
        byte[] encodedBytes = Hex.encode(bytes);
        String requestBody = prefix + new String(encodedBytes);
        String responceStr = postEnclosingRequest(method, RequestType.Post, requestBody);

        try {
            T responce = parse(responceStr);
            return responce;
        } catch (Exception e) {
            throw tryParseResponseException(responceStr, e);
        }
    }

    private IOException tryParseResponseException(String responce, Exception e) {
        String message;
        try {
            JSONObject jso = new JSONObject(responce);
            JSONObject error = jso.getJSONObject("error");
            message = error.getString("message");
        } catch (Exception e1) {
            message = responce;
        }
        if (responce.contains("known transaction:")) {
            return new KnownTransactionException(message);
        }
        if (responce.contains("nonce too low")) {
            return new NonceTooLowException(message);
        }
        if (responce.contains("insufficient funds")) {
            return new LowFundsException(message);
        }
        return new IOException(e);
    }

    protected void handleException(Exception ex) {
        if (debugData != null) {
            debugData.setException(ex).log();
        }

        listener.onNetworkRequestError(this, ex);
    }

    protected byte[] toUnsignedByteArray(BigInteger value) {
        byte[] arr = value.toByteArray();
        int start = 0;
        while (start < arr.length && arr[start] == 0) {
            start++;
        }
        if (start == 0)
            return arr;
        byte[] result = new byte[arr.length - start];
        System.arraycopy(arr, start, result, 0, result.length);
        return result;
    }

    protected abstract T parse(String source) throws IOException, JSONException;

    protected Transaction createTransaction() {
        return null;
    }

    public void cancel() {
        cancelled = true;
    }

    public interface NetworkRequestListener {
        void onNetworkRequestStart(NetworkRequest request);

        void onNetworkRequestDone(NetworkRequest request, Object responce);

        void onNetworkRequestError(NetworkRequest request, Exception e);
    }
}
