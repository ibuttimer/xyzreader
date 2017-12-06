package com.example.xyzreader.remote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class RemoteEndpointUtil {

    private RemoteEndpointUtil() {
    }

    public static JSONArray fetchJsonArray() {
        JSONArray array = null;
        String itemsJson = null;
        try {
            itemsJson = fetchPlainText(Config.BASE_URL);
        } catch (IOException e) {
            Timber.e(e, "Error fetching items JSON");
        }

        // Parse JSON
        if (itemsJson != null) {
            try {
                JSONTokener tokener = new JSONTokener(itemsJson);
                Object val = tokener.nextValue();
                if (!(val instanceof JSONArray)) {
                    throw new JSONException("Expected JSONArray");
                }
                array = (JSONArray) val;
            } catch (JSONException e) {
                Timber.e(e, "Error parsing items JSON");
            }
        }
        return array;
    }

    static String fetchPlainText(URL url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        ResponseBody body = response.body();
        String bodyText;
        if (body != null) {
            bodyText = body.string();
        } else {
            bodyText = null;
        }
        return bodyText;
    }

}
