package com.kaltura.kflow.manager;

import com.kaltura.client.Client;
import com.kaltura.client.Configuration;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.kflow.ui.debug.DebugListener;
import com.kaltura.kflow.utils.AndroidAPIRequestsExecutor;

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
public class PhoenixApiManager {

    private static Client client;

    public static Client getClient() {
        if (client == null) client = new Client(new Configuration());
        return client;
    }

    public static void setDebugListener(DebugListener debugListener) {
        AndroidAPIRequestsExecutor.setDebugListener(debugListener);
    }

    public static void removeDebugListener() {
        AndroidAPIRequestsExecutor.removeDebugListener();
    }

    public static void execute(RequestBuilder<?, ?, ?> requestBuilder) {
        AndroidAPIRequestsExecutor.getExecutor().queue(requestBuilder.build(getClient()));
    }

    public static void cancelAll() {
        AndroidAPIRequestsExecutor.getExecutor().clearRequests();
    }
}