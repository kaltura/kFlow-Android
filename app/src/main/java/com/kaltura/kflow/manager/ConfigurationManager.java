package com.kaltura.kflow.manager;

import com.kaltura.kflow.entity.ConfigurationEntity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
public class ConfigurationManager {

    private static ConfigurationManager sInstance = null;
    private ConfigurationEntity configuration;

    private ConfigurationManager() {
        configuration = new ConfigurationEntity();
    }

    public static ConfigurationManager getInstance() {
        if (sInstance == null) sInstance = new ConfigurationManager();
        return sInstance;
    }

    public void loadConfiguration() {
        ExternalApiManager.getInstance().loadConfiguration(new Callback<ConfigurationEntity>() {
            @Override
            public void onResponse(Call<ConfigurationEntity> call, Response<ConfigurationEntity> response) {
                configuration = response.body();
            }

            @Override
            public void onFailure(Call<ConfigurationEntity> call, Throwable t) {

            }
        });
    }

    public ConfigurationEntity getConfiguration() {
        return configuration;
    }
}
