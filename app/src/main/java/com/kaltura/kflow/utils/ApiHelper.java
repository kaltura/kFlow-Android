package com.kaltura.kflow.utils;

import com.kaltura.client.Client;
import com.kaltura.client.Configuration;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.kflow.Settings;
import com.kaltura.kflow.ui.debug.DebugListener;

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
public class ApiHelper {

    private static Client client;

    public static Client getClient() {
        if (client == null) {
            Configuration config = new Configuration();
            config.setConnectTimeout(10000);
            config.setEndpoint(Settings.host);

            client = new Client(config);
        }
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