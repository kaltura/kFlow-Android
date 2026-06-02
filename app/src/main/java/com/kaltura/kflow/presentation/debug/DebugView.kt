package com.kaltura.kflow.presentation.debug
import android.content.Context
import android.transition.TransitionManager
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.ViewDebugBinding
import com.kaltura.kflow.presentation.extension.invisible
import com.kaltura.kflow.presentation.extension.setColor
import com.kaltura.kflow.presentation.extension.visible
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
        set(value) {
            field = value
            updateServiceUrl()
        }

    var requestMethod: String = ""
    var responseCode = 0

    private var responseJson: JSONObject = JSONObject()
    private var requestJson: JSONObject = JSONObject()
    private var _binding: ViewDebugBinding? = null
    private val binding get() = _binding!!

    init {
        //inflate(R.layout.view_debug, true)
        _binding = ViewDebugBinding.inflate(LayoutInflater.from(context), this,true)
        //binding.requestBody.text = "{\"partnerId\":3201,\"udid\":\"ibKM1Y8TN9pOuocnhU1M6CTEgBrB6iGGcw4OeVaKYcw\",\"ignoreNull\":true,\"format\":1,\"clientTag\":\"java:22-09-21\",\"apiVersion\":\"8.0.0.30025\",\"ks\":\"djJ8MzIwMXyazY2f3NSoOuJzvjDy6dKJ1nUMUQqXtVWiXmoM9bFKfrwm6z1iRYcVv7S35EUobgbgiKPnihtw0Fp8Qa2gPerAoIuO4wcMoI4sq_HmCGs8ECbmJoHw0GpuK48J0tZ39LC-MmwaPeLamAknPg2lMZ9DDL-7VxIxjbgYt1_pMG3fBWnbundZBN7I6yaSzIwsS03ghnKSwak52-Wyr39AkaidEfCPIbrCPLJl6ygExmei5OTOEZYEYa-O4M0FMmd_Wc6oZ-L-3ZmDNXk7fHi0wdcCABmXkurWmzrBW8s5dbFiEVrzhkqT1SZ_TvI2JmRhcaMoxJ-bfLyW0EZLxCGUNvfmAm0uh4qQ2ErjRAKRucnSBaWgACmSMTB6AB8kkY0yPQHSToEkH8gKZqobltqrzyMj\"}"
        binding.requestSort.setOnClickListener {
            if (binding.requestSort.isSelected) {
                binding.requestSort.isSelected = false
                binding.requestSort.drawable.colorFilter = null
            } else {
                binding.requestSort.isSelected = true
                binding.requestSort.drawable.mutate().setColor(R.color.colorAccent)
            }
            setRequestBody(requestJson)
        }
        binding.responseSort.setOnClickListener {
            if (binding.responseSort.isSelected) {
                binding.responseSort.isSelected = false
                binding.responseSort.drawable.colorFilter = null
            } else {
                binding.responseSort.isSelected = true
                binding.responseSort.drawable.mutate().setColor(R.color.colorAccent)
            }
            setResponseBody(responseJson)
        }
    }

    fun setRequestBody(json: JSONObject) {
        try {
            requestJson = json
            TransitionManager.beginDelayedTransition(binding.requestContainer)
            binding.requestBody.text = getRequestJsonAsText()
            binding.requestSort.visible()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun setRequestBodyString(request: String) {
        try {
            TransitionManager.beginDelayedTransition(binding.requestContainer)
            binding.requestBody.text = request
            binding.requestSort.visible()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    fun setResponseBodyString(response: String) {
        try {
            TransitionManager.beginDelayedTransition(binding.requestContainer)
            binding.responseBody.text = response
            binding.requestSort.visible()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun setResponseBody(json: JSONObject) {
        try {
            responseJson = json
            TransitionManager.beginDelayedTransition(binding.responseContainer)
            binding.responseBody.text = getResponseJsonAsText().take(3000)
            binding.responseSort.visible()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun onUnknownError() {
        binding.requestBody.text = ""
        binding.responseBody.text = "Error!"
        binding.requestSort.invisible()
        binding.responseSort.invisible()
    }

    fun clear() {
        requestUrl = ""
        requestMethod = ""
        responseCode = -1
        requestJson = JSONObject()
        responseJson = JSONObject()
        binding.requestBody.text = ""
        binding.responseBody.text = ""
        binding.serviceUrlBody.text = ""
        binding.requestSort.invisible()
        binding.responseSort.invisible()
    }

    val sharedData: String
        get() = "URL $requestUrl\n" +
                "Method $requestMethod\n" +
                "Status $responseCode\n" +
                "Request Body:\n ${getRequestJsonAsText()}\n" +
                "Response Body:\n ${getResponseJsonAsText()}\n"

    private fun getRequestJsonAsText() = if (binding.requestSort.isSelected) requestJson.toString(2) else requestJson.toString()

    private fun getResponseJsonAsText() = if (binding.responseSort.isSelected) responseJson.toString(2) else responseJson.toString()

    private fun updateServiceUrl() {
        val serviceUrl = requestUrl.split("service")
        binding.serviceUrlBody.text = serviceUrl.getOrNull(1) ?: ""
    }
}