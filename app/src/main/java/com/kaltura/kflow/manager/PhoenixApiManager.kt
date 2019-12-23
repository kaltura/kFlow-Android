package com.kaltura.kflow.manager

import com.kaltura.client.Client
import com.kaltura.client.Configuration
import com.kaltura.client.utils.request.BaseRequestBuilder
import com.kaltura.kflow.presentation.debug.DebugListener
import com.kaltura.kflow.utils.AndroidAPIRequestsExecutor

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
object PhoenixApiManager {

    val client = Client(Configuration())

    fun setDebugListener(debugListener: DebugListener) {
        AndroidAPIRequestsExecutor.setDebugListener(debugListener)
    }

    fun removeDebugListener() {
        AndroidAPIRequestsExecutor.removeDebugListener()
    }

    fun execute(requestBuilder: BaseRequestBuilder<*, *>) {
        AndroidAPIRequestsExecutor.getExecutor().queue(requestBuilder.build(client))
    }

    fun cancelAll() {
        AndroidAPIRequestsExecutor.getExecutor().clearRequests()
    }
}