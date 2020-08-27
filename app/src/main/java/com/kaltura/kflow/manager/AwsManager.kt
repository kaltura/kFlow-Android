package com.kaltura.kflow.manager

import android.content.Context
import android.util.Log
import com.amazonaws.AmazonClientException
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserState
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.client.results.SignInResult
import com.amazonaws.mobile.client.results.SignInState
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.iotdata.AWSIotDataClient
import com.amazonaws.services.iotdata.model.GetThingShadowRequest
import com.kaltura.client.types.APIException
import com.kaltura.kflow.utils.Resource
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.util.*
import kotlin.concurrent.thread

/**
 * Created by alex_lytvynenko on 26.08.2020.
 */
class AwsManager(private val context: Context) {

    private val client by lazy {
        AWSIotDataClient(AWSMobileClient.getInstance().credentials).apply {
            setRegion(Region.getRegion(Regions.US_WEST_2))
        }
    }

    private lateinit var mqttManager: AWSIotMqttManager

    fun setAwsClientEndpoint(iotEndPoint: String) {
        thread {
            client.endpoint = iotEndPoint
        }
    }

    //Init And Get AWS Configuration
    fun startAwsInitProcess(config: JSONObject) {
        val configuration = AWSConfiguration(config)
        AWSMobileClient.getInstance().initialize(context, configuration, object : Callback<UserStateDetails> {
            override fun onResult(userStateDetails: UserStateDetails) {
                when (userStateDetails.userState) {
                    UserState.SIGNED_IN -> AWSMobileClient.getInstance().signOut()
                    UserState.SIGNED_OUT -> Unit
                    UserState.GUEST -> Unit
                    else -> AWSMobileClient.getInstance().signOut()
                }
            }

            override fun onError(e: Exception) {
                Log.e("INIT", e.toString())
            }
        })
    }

    fun signInAws(user: String, pwd: String, awsListener: (state: Resource<SignInState>) -> Unit = {}) {
        if (AWSMobileClient.getInstance().currentUserState().userState != UserState.SIGNED_IN)
            AWSMobileClient.getInstance().signIn(user, pwd, null, object : Callback<SignInResult> {
                override fun onResult(signInResult: SignInResult) {
                    awsListener(Resource.Success(signInResult.signInState))
                }

                override fun onError(e: java.lang.Exception) {
                    awsListener(Resource.Error(APIException(e)))
                }
            })
        else awsListener(Resource.Success(SignInState.DONE))
    }

    //IOT Service Section
    fun initMqtt(iotEndPoint: String) {
        mqttManager = AWSIotMqttManager(UUID.randomUUID().toString(), iotEndPoint)
    }

    fun getThingShadow(thingName: String, awsListener: (state: Resource<String>) -> Unit = {}) {
        thread {
            val result = try {
                val getThingShadowRequest = GetThingShadowRequest().withThingName(thingName)
                client.getThingShadow(getThingShadowRequest)
            } catch (e: AmazonClientException) {
                e.printStackTrace()
                awsListener(Resource.Error(APIException(e)))
                return@thread
            }
            val bytes = ByteArray(result.payload.remaining())
            result.payload[bytes]
            awsListener(Resource.Success(String(bytes)))
        }
    }

    fun connect(listener: (status: Resource<AWSIotMqttClientStatus>) -> Unit) {
        try {
            mqttManager.connect(AWSMobileClient.getInstance()) { status, throwable ->
                listener(Resource.Success(status))
            }
        } catch (e: java.lang.Exception) {
            listener(Resource.Error(APIException(e)))
        }
    }

    fun subscribeToTopic(cognitoTopicName: String, listener: (state: Resource<String>) -> Unit = {}) {
        thread {
            try {
                mqttManager.subscribeToTopic(cognitoTopicName, AWSIotMqttQos.QOS0 /* Quality of Service */) { topic, data ->
                    try {
                        val message = String(data)
                        listener(Resource.Success(message))
                    } catch (e: UnsupportedEncodingException) {
                        listener(Resource.Error(APIException(e)))
                    }
                }
            } catch (e: java.lang.Exception) {
                listener(Resource.Error(APIException(e)))
            }
        }
    }

    fun subscribeToTopicShadowAccepted(thingName: String, listener: (state: Resource<String>) -> Unit = {}) {
        try {
            val shadowTopic = "\$aws/things/$thingName/shadow/update/accepted"
            mqttManager.subscribeToTopic(shadowTopic, AWSIotMqttQos.QOS0 /* Quality of Service */) { topic, data ->
                try {
                    val message = String(data)
                    listener(Resource.Success(message))
                } catch (e: UnsupportedEncodingException) {
                    listener(Resource.Error(APIException(e)))
                }
            }
        } catch (e: java.lang.Exception) {
            listener(Resource.Error(APIException(e)))
        }
    }
}