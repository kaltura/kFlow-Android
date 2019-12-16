package com.kaltura.kflow.presentation.debug

import org.json.JSONObject

/**
 * Created by alex_lytvynenko on 20.11.2018.
 */
interface DebugListener {
    fun setRequestInfo(url: String, method: String, code: Int)
    fun setRequestBody(requestBody: JSONObject)
    fun setResponseBody(responseBody: JSONObject)
    fun onError()
}