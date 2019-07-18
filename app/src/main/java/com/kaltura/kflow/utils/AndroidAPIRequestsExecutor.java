package com.kaltura.kflow.utils;

import android.os.Handler;
import android.os.Looper;

import com.kaltura.client.APIOkRequestsExecutor;
import com.kaltura.client.utils.request.ConnectionConfiguration;
import com.kaltura.client.utils.request.RequestElement;
import com.kaltura.client.utils.response.base.ResponseElement;
import com.kaltura.kflow.presentation.debug.DebugListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alex_lytvynenko on 20.11.2018.
 */
public class AndroidAPIRequestsExecutor extends APIOkRequestsExecutor {

    private static AndroidAPIRequestsExecutor mainExecutor;
    private static DebugListener debugListener;
    private Handler handler = null;

    public static AndroidAPIRequestsExecutor getExecutor() {
        if (mainExecutor == null) {
            mainExecutor = new AndroidAPIRequestsExecutor(new Handler(Looper.getMainLooper()));
        }
        return mainExecutor;
    }

    public AndroidAPIRequestsExecutor() {
        super();
    }

    public AndroidAPIRequestsExecutor(Handler handler) {
        super();
        this.handler = handler;
    }

    public AndroidAPIRequestsExecutor(ConnectionConfiguration defaultConfiguration) {
        super(defaultConfiguration);
    }

    @Override
    public String queue(RequestElement requestElement) {
        try {
            return super.queue(requestElement);
        } catch (Exception ex) {
            if (debugListener != null) {
                try {
                    debugListener.setRequestInfo(requestElement.getUrl(), requestElement.getMethod(), -1);
                    debugListener.setRequestBody(new JSONObject(requestElement.getBody()));
                    debugListener.setResponseBody(new JSONObject("{\"error\":" + "\"" + ex.toString() + "\"}"));
                } catch (JSONException e) {
                    debugListener.onError();
                    debugListener.onError();
                    e.printStackTrace();
                }
            }
            return "";
        }
    }

    @Override
    protected void postCompletion(final RequestElement action, final ResponseElement responseElement) {
        final com.kaltura.client.utils.response.base.Response<?> apiResponse = action.parseResponse(responseElement);

        if (handler != null) {
            handler.post(() -> {
                provideDebugData(action, responseElement);
                action.onComplete(apiResponse);
            });
        } else {
            provideDebugData(action, responseElement);
            action.onComplete(apiResponse);
        }
    }

    public static void setDebugListener(DebugListener debugListener) {
        AndroidAPIRequestsExecutor.debugListener = debugListener;
    }

    public static void removeDebugListener() {
        AndroidAPIRequestsExecutor.debugListener = null;
    }

    private void provideDebugData(final RequestElement action, ResponseElement responseElement) {
        if (debugListener != null) {
            try {
                debugListener.setRequestInfo(action.getUrl(), action.getMethod(), responseElement.getCode());
                debugListener.setRequestBody(new JSONObject(action.getBody()));
                debugListener.setResponseBody(new JSONObject(responseElement.getResponse()));
            } catch (JSONException e) {
                debugListener.onError();
                debugListener.onError();
                e.printStackTrace();
            }
        }
    }
}
