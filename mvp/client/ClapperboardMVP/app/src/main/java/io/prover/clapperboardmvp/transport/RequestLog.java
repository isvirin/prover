package io.prover.clapperboardmvp.transport;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.prover.clapperboardmvp.transport.NetworkRequest.TAG;

/**
 * Created by babay on 07.09.2017.
 */

public class RequestLog {
    private final RequestType requestType;
    public boolean storeDebug = true;
    String url;
    String requestBody;
    Exception exception;
    long createTime;
    long requestStartTime;
    long requestEndTime;
    private HashMap<String, String> headers;
    private HashMap<String, String> fields;
    private String responce;

    public RequestLog(RequestType requestType) {
        this.requestType = requestType;
        createTime = System.currentTimeMillis();
    }

    public void onStart(HttpURLConnection urlConnection) {
        if (storeDebug) {
            url = urlConnection.getURL().toString();
            Map<String, List<String>> props = urlConnection.getRequestProperties();
            headers = new HashMap<>();
            StringBuilder builder = new StringBuilder();
            for (String key : props.keySet()) {
                builder.replace(0, builder.length(), "");
                List<String> values = props.get(key);
                for (String value : values) {
                    if (builder.length() > 0)
                        builder.append(", ");
                    builder.append(value);
                }
                headers.put(key, builder.toString());
            }
            Log.d(TAG, "sending request" + toString());
        }
        requestStartTime = System.currentTimeMillis();


    }

    public void onGotResponse(String result) {
        requestEndTime = System.currentTimeMillis();
        if (storeDebug) {
            responce = result;
        }
    }

    public void appendFormField(String name, String value) {
        if (fields == null)
            fields = new HashMap<>();
        fields.put(name, value);
    }

    public void appendFile(String fieldName, File uploadFile) {
        if (fields == null)
            fields = new HashMap<>();
        fields.put(fieldName, "file: " + uploadFile.getPath());
    }

    public RequestLog setException(Exception e) {
        exception = e;
        return this;
    }

    public void log() {
        if (exception == null) {
            Log.d(TAG, toString());
        } else {
            Log.e(TAG, toString());
            Log.e(TAG, exception.getMessage(), exception);
        }
    }

    @Override
    public String toString() {
        return toStringBuilder().toString();
    }

    private StringBuilder toStringBuilder() {
        StringBuilder builder = new StringBuilder();
        builder.append(requestType.name()).append(" request: ").append(url).append("\n");
        if (headers != null && headers.size() > 0) {
            for (String key : headers.keySet()) {
                builder.append(key).append(": ").append(headers.get(key)).append("\n");
            }
        }
        if (fields != null && fields.size() > 0) {
            for (String key : fields.keySet()) {
                builder.append(key).append(": ").append(fields.get(key)).append("\n");
            }
        }
        if (requestBody != null)
            builder.append("request: ").append(requestBody).append("\n");
        if (requestEndTime != 0) {
            builder.append("response time: ").append(requestEndTime - requestStartTime)
                    .append("; response: ").append(responce).append("\n");
        }
        return builder;
    }

    public void logToFile(File file) {
        StringBuilder builder = toStringBuilder();
        if (exception != null) {
            builder.append(Log.getStackTraceString(exception));
            builder.append(TextUtils.join("\n", exception.getStackTrace()));
        }

        try (FileOutputStream stream = new FileOutputStream(file, true)) {
            stream.write("\n\n".getBytes());
            stream.write(builder.toString().getBytes());
            stream.write("\n\n".getBytes());
            stream.flush();
            stream.close();
        } catch (IOException e) {
            Log.e("error", e.getMessage(), e);
        }
    }
}
