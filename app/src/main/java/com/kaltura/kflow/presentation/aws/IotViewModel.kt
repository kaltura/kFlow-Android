package com.kaltura.kflow.presentation.aws

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.amazonaws.mobile.client.results.SignInState
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.kaltura.client.services.EpgService
import com.kaltura.client.services.IotService
import com.kaltura.client.types.*
import com.kaltura.kflow.manager.AwsManager
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

/**
 * Created by alex_lytvynenko on 2020-01-14.
 */
class IotViewModel(private val apiManager: PhoenixApiManager,
                   private val preferenceManager: PreferenceManager,
                   private val awsManager: AwsManager) : BaseViewModel(apiManager) {

    private var IOT_INTERVAL = 1
    private var announcementTopic = ""
    private var epgUpdateTopic = ""
    private lateinit var topicList : List<String>
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
        registerIot()
    }

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
        topicList.forEach { topic ->
            awsManager.subscribeToEPGUpdates(topic) {
                IOTepgMessageEvent.postValue(it)
            }
        }
    }

    fun subscribeToThingShadow() {
        awsManager.subscribeToTopicShadowAccepted(iotThing) {
            IOTshadowMessageEvent.postValue(it)
        }
    }

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

    private fun getClientConfigIot() {
        apiManager.execute(IotService.GetClientConfigurationIotBuilder().setCompletion {
            if (it.isSuccess) {
                if (it.results.status.equals("Success2", true)) {
                    var iot = it.results
                    saveIotInfo(iot)
                    val awsConfig = awsManager.updatedAWSConfig(it.results)
                    awsManager.startAwsInitProcess(awsConfig) {
                        if (it.isSuccess()) {
                            topicList = obtainTopicList(iot)
                            signInAws()
                        } else
                            registrationEvent.postValue(Resource.Error(it.getErrorData()))
                    }

                }else{
                    if (IOT_INTERVAL <= awsManager.getIntervalCount()){
                        Log.d("elad","Interval number : "+IOT_INTERVAL)
                        Log.d("elad","New Config will be called at : "+awsManager.getIntervalTimeout())
                        Timer("IOT Registration Intervals", false).schedule(awsManager.getIntervalTimeout()) {
                            getClientConfigIot()
                            IOT_INTERVAL++
                        }
                    }else{
                        it.error = APIException()
                        registrationEvent.postValue(Resource.Error(it.error))
                        IOT_INTERVAL = 1
                    }
                }
            }
            else
                registrationEvent.value = Resource.Error(it.error)
            })
        }


    private fun obtainTopicList(results: IotClientConfiguration?): List<String> {
        return results?.topics?.split(",")?.toTypedArray()!!?.toList()
    }

    private fun registerIot() {
        apiManager.execute(IotService.RegisterIotBuilder().setCompletion {
            if (it.isSuccess) {
                getClientConfigIot()
            }
            else
                registrationEvent.postValue(Resource.Error(it.error))
        })

    }

    private fun getEPGUpdatesTopic(clientConfiguration: IotClientConfiguration) : String{

        try {
            Log.d("TEST","Register to topic : "+clientConfiguration.topics.split(",").get(0))
            return clientConfiguration.topics.split(",").get(0);
        } catch (e: Exception) {
            Log.d("TEST","Register to topic : epg_update_5")
            return "epg_update_FAILED"
        }

    }

    private fun saveIotInfo(iot: IotClientConfiguration) {
        iotThing = iot.thingName//getThingName(iot.thingArn)
        iotEndpoint = getATSIotEndpoint(iot.endPoint)//iot.endPoint
        preferenceManager.iotThing = iotThing
        preferenceManager.iotEndpoint = iotEndpoint
        preferenceManager.iotUsername = iot.username
        preferenceManager.iotPassword = iot.password
    }

    private fun getATSIotEndpoint(legacyEndPoint:String):String{

        return legacyEndPoint.replace("wss://","").replace("/mqtt","")

        //TO DO
        /* BE FIX Required to provide valid ATS End Point
         Chae iotEndpoint Format to suppurt Data-ATS
         "aycb5f0u1fyvg-ats.iot.eu-central-1.amazonaws.com"
        */


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
}