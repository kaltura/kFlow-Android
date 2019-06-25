package com.kaltura.kflow.manager;

import com.kaltura.kflow.entity.ConfigurationEntity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
public class ExternalApiManager {

    private static ExternalApiManager sInstance = null;
    private ExternalService service;

    private ExternalApiManager() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://54.244.95.154/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ExternalService.class);
    }

    public static ExternalApiManager getInstance() {
        if (sInstance == null) sInstance = new ExternalApiManager();
        return sInstance;
    }

    void loadConfiguration(Callback<ConfigurationEntity> callback) {
        service.getConfiguration().enqueue(callback);
    }

    public interface ExternalService {

        @GET("SUS/configuration.json")
        Call<ConfigurationEntity> getConfiguration();
    }
}
