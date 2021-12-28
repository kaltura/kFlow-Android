package com.kaltura.kflow.manager

import com.kaltura.client.Client
import com.kaltura.client.Configuration
import com.kaltura.client.utils.request.BaseRequestBuilder
import com.kaltura.kflow.presentation.debug.DebugListener
import com.kaltura.kflow.utils.AndroidAPIRequestsExecutor

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class PhoenixApiManager(private val prefs: PreferenceManager) {

    private val client = Client(Configuration())

    init {
        val config = Configuration().apply { endpoint = prefs.baseUrl }
        client.connectionConfiguration = config
        client.ks = prefs.ks
    }

    var ks: String?
        get() = client.ks
        set(value) {
            client.ks = value
        }

    var parthnerID: Int?
        get() = client.partnerId
        set(value) {
            client.partnerId = value
        }

    var cloudfrontUrl: String?
        get() = prefs.cloudeFrontUrl
        set(value) {
            prefs.cloudeFrontUrl = value.toString()
        }

    fun setConnectionConfiguration(config: Configuration) {
        client.connectionConfiguration = config
    }

    fun setDebugListener(debugListener: DebugListener) {
        AndroidAPIRequestsExecutor.setDebugListener(debugListener)
    }

    fun removeDebugListener() {
        AndroidAPIRequestsExecutor.removeDebugListener()
    }

    fun execute(requestBuilder: BaseRequestBuilder<*, *>) {
        AndroidAPIRequestsExecutor.queue(requestBuilder.build(client))
    }

    fun cancelAll() {
        AndroidAPIRequestsExecutor.clearRequests()
    }
}