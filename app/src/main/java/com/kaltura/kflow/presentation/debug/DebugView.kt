package com.kaltura.kflow.presentation.debug

import android.content.Context
import android.graphics.PorterDuff
import android.transition.TransitionManager
import android.util.AttributeSet
import android.widget.LinearLayout
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.extension.getColor
import com.kaltura.kflow.presentation.extension.inflate
import com.kaltura.kflow.presentation.extension.invisible
import com.kaltura.kflow.presentation.extension.visible
import kotlinx.android.synthetic.main.view_debug.view.*
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by alex_lytvynenko on 20.11.2018.
 */
class DebugView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    var requestUrl: String = ""
    var requestMethod: String = ""
    var responseCode = 0
    private var requestJson: JSONObject = JSONObject()
    private var responseJson: JSONObject = JSONObject()

    init {
        inflate(R.layout.view_debug, true)
        requestSort.setOnClickListener {
            if (requestSort.isSelected) {
                requestSort.isSelected = false
                requestSort.drawable.colorFilter = null
            } else {
                requestSort.isSelected = true
                requestSort.drawable.mutate().setColorFilter(getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
            }
            setRequestBody(requestJson)
        }
        responseSort.setOnClickListener {
            if (responseSort.isSelected) {
                responseSort.isSelected = false
                responseSort.drawable.colorFilter = null
            } else {
                responseSort.isSelected = true
                responseSort.drawable.mutate().setColorFilter(resources.getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
            }
            setResponseBody(responseJson)
        }
    }

    fun setRequestBody(json: JSONObject) {
        try {
            requestJson = json
            val text = if (requestSort.isSelected) json.toString(2) else json.toString()
            TransitionManager.beginDelayedTransition(requestContainer)
            requestBody.text = text
            requestSort.visible()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun setResponseBody(json: JSONObject) {
        try {
            responseJson = json
            val text = if (responseSort.isSelected) json.toString(2) else json.toString()
            TransitionManager.beginDelayedTransition(responseContainer)
            responseBody.text = text
            responseSort.visible()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun onUnknownError() {
        requestBody.text = ""
        responseBody.text = "Error!"
        requestSort.invisible()
        responseSort.invisible()
    }

    fun clear() {
        requestUrl = ""
        requestMethod = ""
        responseCode = -1
        requestJson = JSONObject()
        responseJson = JSONObject()
        requestBody.text = ""
        responseBody.text = ""
        requestSort.invisible()
        responseSort.invisible()
    }

    val sharedData: String
        get() = "URL $requestUrl\n" +
                "Method $requestMethod\n" +
                "Status $responseCode\n" +
                "Request Body:\n ${requestBody.text}\n" +
                "Response Body:\n ${responseBody.text}\n"
}