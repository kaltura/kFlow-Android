package com.kaltura.kflow.ui.debug;

import org.json.JSONObject;

/**
 * Created by alex_lytvynenko on 20.11.2018.
 */
public interface DebugListener {
    void setRequestInfo(String url, String method, int code);

    void setRequestBody(JSONObject requestBody);

    void setResponseBody(JSONObject responseBody);

    void onError();
}
