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
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kaltura.client.types.APIException
import com.kaltura.client.types.IotClientConfiguration
import com.kaltura.kflow.utils.Resource
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.util.*
import kotlin.concurrent.thread


/**
 * Created by alex_lytvynenko on 26.08.2020.
 */
class AwsManager(private val context: Context) {

    var INTERVAL_COUNT = 3
    var INTERVAL_TIMEOUT = 10000L

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

    fun getIntervalCount():Int{
        return INTERVAL_COUNT
    }
    fun getIntervalTimeout():Long{
        return INTERVAL_TIMEOUT
    }

    //Init And Get AWS Configuration
    fun startAwsInitProcess(config: JSONObject, awsListener: (state: Resource<UserState>) -> Unit = {}) {
        val configuration = AWSConfiguration(config)
        AWSMobileClient.getInstance().initialize(context, configuration, object : Callback<UserStateDetails> {
            override fun onResult(userStateDetails: UserStateDetails) {
                when (userStateDetails.userState) {
                    UserState.SIGNED_IN -> {
                        AWSMobileClient.getInstance().signOut()
                        awsListener(Resource.Success(UserState.SIGNED_IN))
                    }
                    UserState.SIGNED_OUT -> {
                        awsListener(Resource.Success(UserState.SIGNED_OUT))
                    }
                    UserState.GUEST -> {
                        awsListener(Resource.Success(UserState.GUEST))
                    }
                    else -> {
                        AWSMobileClient.getInstance().signOut()
                        awsListener(Resource.Success(UserState.SIGNED_OUT))
                    }
                }
            }

            override fun onError(e: Exception) {
                Log.e("INIT", e.toString())
                awsListener(Resource.Error(APIException(e)))
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

    fun subscribeToEPGUpdates(EPGTopicName: String, listener: (state: Resource<String>) -> Unit = {}) {
        thread {
            try {
                mqttManager.subscribeToTopic(EPGTopicName, AWSIotMqttQos.QOS0 /* Quality of Service */) { topic, data ->
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

    fun updatedAWSConfig(iot: IotClientConfiguration):JSONObject {

        val inputStream: InputStream = context.assets.open("awsconfiguration.json")

        var json: JsonObject
        json = try {
            val element: JsonElement = JsonParser().parse(
                InputStreamReader(inputStream)
            )
            element.asJsonObject
        } catch (e: IOException) {
            throw RuntimeException(e.getLocalizedMessage())
        }

        val cognitoIdentityJSON: JsonObject? = json.getAsJsonObject("CredentialsProvider").
        getAsJsonObject("CognitoIdentity").getAsJsonObject("Default")

        cognitoIdentityJSON?.addProperty("PoolId",iot.identityPoolId)
        cognitoIdentityJSON?.addProperty("Region",iot.awsRegion)
        cognitoIdentityJSON?.addProperty("AppClientId",iot.appClientId)

        val cognitoUserPoolJSON: JsonObject? = json.getAsJsonObject("CognitoUserPool").getAsJsonObject("Default")
        cognitoUserPoolJSON?.addProperty("PoolId",iot.userPoolId)
        cognitoUserPoolJSON?.addProperty("Region",iot.awsRegion)
        cognitoUserPoolJSON?.addProperty("AppClientId",iot.appClientId)

        json.getAsJsonObject("CredentialsProvider").getAsJsonObject("CognitoIdentity").remove("Default")
        json.getAsJsonObject("CredentialsProvider").getAsJsonObject("CognitoIdentity").add("Default",cognitoIdentityJSON)

        json.getAsJsonObject("CognitoUserPool").remove("Default")
        json.getAsJsonObject("CognitoUserPool").add("Default",cognitoUserPoolJSON)

        return JSONObject(json.toString())



    }
}