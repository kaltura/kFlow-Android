package com.kaltura.kflow.presentation.aws
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.amazonaws.mobile.client.results.SignInState
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.kaltura.client.services.EpgService
import com.kaltura.client.services.IotService
import com.kaltura.client.types.*
import com.kaltura.kflow.entity.EPGProgram
import com.kaltura.kflow.manager.AwsManager
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by alex_lytvynenko on 2020-01-14.
 */
class IotViewModel(private val apiManager: PhoenixApiManager,
                   private val preferenceManager: PreferenceManager,
                   private val awsManager: AwsManager) : BaseViewModel(apiManager) {

    private var announcementTopic = ""
    private var epgUpdateTopic = ""
    private var iotThing = preferenceManager.iotThing
    private var iotEndpoint = preferenceManager.iotEndpoint
    private var iotUsername = preferenceManager.iotUsername
    private var iotPassword = preferenceManager.iotPassword

    val registrationEvent = SingleLiveEvent<Resource<SignInState>>()
    val connectEvent = SingleLiveEvent<Resource<AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus>>()
    val IOTshadowMessageEvent = SingleLiveEvent<Resource<String>>()
    val IOTannouncementMessageEvent = SingleLiveEvent<Resource<String>>()
    val IOTepgMessageEvent = SingleLiveEvent<Resource<String>>()
    val epgUpdates = MutableLiveData<Resource<ArrayList<Epg>>>()

    fun register() {
        getClientConfigIot()
    }
    fun getKs() = apiManager.ks

    fun getCloudfrontUrl() = apiManager.cloudfrontUrl

    fun getParthnerID() = apiManager.parthnerID

    fun initMqtt() {
        awsManager.initMqtt(iotEndpoint)
    }

    fun connect() {
        awsManager.getThingShadow(iotThing) {
            if (it.isSuccess())
                IOTshadowMessageEvent.postValue(it)

            awsManager.connect {
                connectEvent.postValue(it)
            }
        }
    }

    fun subscribeToTopicAnnouncement() {
        awsManager.subscribeToTopic(announcementTopic) {
            IOTannouncementMessageEvent.postValue(it)
        }
    }

    fun subscribeToEPGUpdates() {
        awsManager.subscribeToEPGUpdates(epgUpdateTopic) {
            IOTepgMessageEvent.postValue(it)
        }
    }

    fun subscribeToThingShadow() {
        awsManager.subscribeToTopicShadowAccepted(iotThing) {
            IOTshadowMessageEvent.postValue(it)
        }
    }

    private fun getClientConfigIot() {
        apiManager.execute(IotService.GetClientConfigurationIotBuilder().setCompletion {
            if (it.isSuccess) registerIot(it.results)
            else registrationEvent.value = Resource.Error(it.error)
        })
    }

    private fun registerIot(clientConfiguration: IotClientConfiguration) {
        announcementTopic = clientConfiguration.announcementTopic
        epgUpdateTopic = getEPGUpdatesTopic(clientConfiguration)
        startAwsInitProcess(JSONObject(clientConfiguration.json))

        if (iotThing.isNotEmpty() && iotEndpoint.isNotEmpty() && iotUsername.isNotEmpty() && iotPassword.isNotEmpty()) {
            signInAws()
        } else {
            apiManager.execute(IotService.RegisterIotBuilder().setCompletion {
                if (it.isSuccess) {
                    val iot = it.results
                    saveIotInfo(iot)
                    signInAws()
                } else registrationEvent.postValue(Resource.Error(it.error))
            })
        }
    }

    private fun getEPGUpdatesTopic(clientConfiguration: IotClientConfiguration) : String{

        try {
            Log.d("TEST","Register to topic : "+clientConfiguration.topics.split(",").get(0))
            return clientConfiguration.topics.split(",").get(0);
        } catch (e: Exception) {
            Log.d("TEST","topics field is invalid")
            return "epg_update_FAILED"
        }

    }

    private fun saveIotInfo(iot: Iot) {
        iotThing = getThingName(iot.thingArn)
        iotEndpoint = iot.endPoint
        preferenceManager.iotThing = iotThing
        preferenceManager.iotEndpoint = iotEndpoint
        preferenceManager.iotUsername = iot.username
        preferenceManager.iotPassword = iot.userPassword
    }

    private fun startAwsInitProcess(json: JSONObject) {
        awsManager.startAwsInitProcess(json)
    }

    private fun signInAws() {
        awsManager.setAwsClientEndpoint(iotEndpoint)
        awsManager.signInAws(preferenceManager.iotUsername, preferenceManager.iotPassword) {
            if (it.isSuccess()) {
                registrationEvent.postValue(Resource.Success(it.getSuccessData()))
            } else registrationEvent.postValue(Resource.Error(it.getErrorData()))
        }
    }

    private fun getThingName(thingARN: String): String {
        if (thingARN.isNotEmpty())
            return thingARN.substring(thingARN.indexOf("thing") + 6)
        return ""
    }
    // EPG display due to IOT update notification [Legacy Service]

    fun getEpgUpdates(liveAssetId: Long) {
        val todayMidnightCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            this[Calendar.MILLISECOND] = 0
            this[Calendar.SECOND] = 0
            this[Calendar.MINUTE] = 0
            this[Calendar.HOUR_OF_DAY] = 0
        }

        val filter = EpgFilter().apply {
            dateEqual = todayMidnightCalendar.timeInMillis / 1000 //Need to be date since beginning of the day 00:00:00
            liveAssetIdEqual = liveAssetId
        }

        apiManager.execute(EpgService.list(filter).setCompletion {
            if (it.isSuccess && it.results.objects != null) {
                if (it.results.objects.size > 0) epgUpdates.value = Resource.Success(it.results.objects as ArrayList<Epg>)
                else epgUpdates.value = Resource.Success(arrayListOf())
            } else epgUpdates.value = Resource.Error(it.error)
        })
    }

    fun callCloudfront(
        url: URL,
        ks: String,
        channel:String,
        listener: (status: Boolean, message: String, data: List<EPGProgram>) -> Unit
    ) {
        Thread {
            try {
                url
                val conn = url.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization","Bearer "+ks)

                val isSuccess = conn.responseCode == 200
                if (isSuccess) {
                    val data = conn.inputStream.bufferedReader().readText()
                    var epgList = parseJson(data,channel)
                    listener(true, "success",epgList)
                } else {
                    listener(false, "Communication Error", emptyList())
                }
            } catch (e: Exception) {
                listener(false, "Json Struct Error", emptyList())
            }
        }.start()
    }

    private fun parseJson(jsonString: String, channelID:String) : List<EPGProgram>{
        var result = ArrayList<EPGProgram>()
        try {
            val jsonObject = JsonParser().parse(jsonString) as JsonObject
            val jsonArray = jsonObject.getAsJsonObject("epgChunk").getAsJsonArray(channelID)

            for (i in 0 until jsonArray.size()) {
                val item = jsonArray[i].asJsonObject
                val startDate = item.get("startDate").asLong
                val endDate = item.get("endDate").asLong
                val name = item.get("name").asString
                val id = item.get("id").asLong.toString()

                val program = EPGProgram(name,startDate,endDate,id)
                result.add(program)

            }

            return result;

        } catch (ex: JsonSyntaxException) {
            return result
        }
    }
}