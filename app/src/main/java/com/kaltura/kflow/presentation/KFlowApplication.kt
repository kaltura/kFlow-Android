package com.kaltura.kflow.presentation

import android.app.Application
import com.kaltura.client.Configuration
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager

/**
 * Created by alex_lytvynenko on 2019-06-25.
 */
class KFlowApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initApiClient()
    }

    private fun initApiClient() {
        val config = Configuration().apply { endpoint = PreferenceManager.with(this@KFlowApplication).baseUrl }
        PhoenixApiManager.client.connectionConfiguration = config
        PhoenixApiManager.client.ks = PreferenceManager.with(this).ks
    }
}