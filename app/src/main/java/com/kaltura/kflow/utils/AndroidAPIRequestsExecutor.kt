package com.kaltura.kflow.utils

import android.os.Handler
import android.os.Looper
import com.kaltura.client.APIOkRequestsExecutor
import com.kaltura.client.utils.request.RequestElement
import com.kaltura.client.utils.response.base.ResponseElement
import com.kaltura.kflow.presentation.debug.DebugListener
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by alex_lytvynenko on 20.11.2018.
 */
object AndroidAPIRequestsExecutor : APIOkRequestsExecutor() {
    private var debugListener: DebugListener? = null
    private var handler: Handler = Handler(Looper.getMainLooper())

    fun setDebugListener(listener: DebugListener) {
        debugListener = listener
    }

    fun removeDebugListener() {
        debugListener = null
    }

    override fun queue(requestElement: RequestElement<*>): String =
            try {
                super.queue(requestElement)
            } catch (ex: Exception) {
                debugListener?.let {
                    try {
                        it.setRequestInfo(requestElement.url, requestElement.method, -1)
                        it.setRequestBody(JSONObject(requestElement.body))
                        it.setResponseBody(JSONObject("{\"error\":\"$ex\"}"))
                    } catch (e: JSONException) {
                        it.onError()
                        e.printStackTrace()
                    }
                }
                ""
            }

    override fun postCompletion(action: RequestElement<Any>, responseElement: ResponseElement) {
        val apiResponse = action.parseResponse(responseElement)
        handler.post {
            provideDebugData(action, responseElement)
            action.onComplete(apiResponse)
        }
    }

    private fun provideDebugData(action: RequestElement<*>, responseElement: ResponseElement) {
        debugListener?.let {
            try {
                it.setRequestInfo(action.url, action.method, responseElement.code)
                it.setRequestBody(JSONObject(action.body))
                it.setResponseBody(JSONObject(responseElement.response))
            } catch (e: JSONException) {
                it.onError()
                e.printStackTrace()
            }
        }
    }
}