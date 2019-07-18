package com.kaltura.kflow.presentation;

import android.app.Application;

import com.kaltura.client.Configuration;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.manager.PreferenceManager;

/**
 * Created by alex_lytvynenko on 2019-06-25.
 */
public class KFlowApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initApiClient();
    }

    private void initApiClient() {
        Configuration config = new Configuration();
        config.setEndpoint(PreferenceManager.getInstance(this).getBaseUrl());
        PhoenixApiManager.getClient().setConnectionConfiguration(config);
        PhoenixApiManager.getClient().setKs(PreferenceManager.getInstance(this).getKs());
    }
}
