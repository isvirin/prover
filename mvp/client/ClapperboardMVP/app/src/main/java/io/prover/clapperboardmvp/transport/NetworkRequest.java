package io.prover.clapperboardmvp.transport;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import io.prover.clapperboardmvp.BuildConfig;
import io.prover.clapperboardmvp.Const;

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
        } catch (Exception ex) {
            handleException(ex);
        }
    }


    protected void handleException(Exception ex) {
        if (debugData != null) {
            debugData.setException(ex).log();
        }

        listener.onNetworkRequestError(this, ex);
    }

    protected abstract T parse(String source) throws IOException, JSONException;

    public void cancel() {
        cancelled = true;
    }

    public interface NetworkRequestListener {
        void onNetworkRequestStart(NetworkRequest request);

        void onNetworkRequestDone(NetworkRequest request, Object responce);

        void onNetworkRequestError(NetworkRequest request, Exception e);
    }
}
