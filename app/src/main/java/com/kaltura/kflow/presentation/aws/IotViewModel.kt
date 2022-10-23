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
import com.kaltura.kflow.R
import com.kaltura.kflow.entity.ChannelCS
import com.kaltura.kflow.entity.EPGProgram
import com.kaltura.kflow.manager.AwsManager
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.presentation.extension.getQuantityString
import com.kaltura.kflow.presentation.extension.longToast
import com.kaltura.kflow.presentation.extension.visible
import com.kaltura.kflow.utils.*
import kotlinx.android.synthetic.main.fragment_iot.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
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
    private lateinit var topicList : List<KeyValue>
    private var iotThing = preferenceManager.iotThing
    private var iotEndpoint = preferenceManager.iotEndpoint

    val registrationEvent = SingleLiveEvent<Resource<SignInState>>()
    val connectEvent = SingleLiveEvent<Resource<AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus>>()
    val IOTshadowMessageEvent = SingleLiveEvent<Resource<String>>()
    val IOTannouncementMessageEvent = SingleLiveEvent<Resource<String>>()
    val IOTepgMessageEvent = SingleLiveEvent<Resource<String>>()
    val IOTLineupMessageEvent = SingleLiveEvent<Resource<String>>()
    val epgUpdates = MutableLiveData<Resource<ArrayList<Epg>>>()

    fun register() {
        registerIot()
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

    fun subscribeToTopicAnnouncement(announcmentTopic:String) {
        awsManager.subscribeToTopic(announcmentTopic) {
            Log.d("TEST","Announcement Received")
            IOTannouncementMessageEvent.postValue(it)
        }
    }

    fun subscribeToAvailableTopics() {
        topicList.forEach { topic ->

            when (topic.key) {
                "SystemAnnouncement" -> {
                    subscribeToTopicAnnouncement(topic.value)
                    Log.d("IOT","IOT System Announcement Topic Registered : "+topic.value)
                }
                "EPG" -> {
                    subscribeToEPGUpdates(topic.value)
                    Log.d("IOT","IOT Eog Update Topic Registered : "+topic.value)
                }
                "Lineup" -> subscribeToLineupUpdates(topic.value)
            }
        }
    }

    fun subscribeToEPGUpdates(epgUpdateTopic:String) {
        awsManager.subscribeToEPGUpdates(epgUpdateTopic) {
            Log.d("TEST","IOT Epg Update Received")
            IOTepgMessageEvent.postValue(it)
        }
    }

    fun subscribeToLineupUpdates(lineupUpdateTopic:String) {
        awsManager.subscribeToLineupUpdates(lineupUpdateTopic) {
            Log.d("TEST","IOT Lineup Update Received")
            IOTLineupMessageEvent.postValue(it)
        }
    }

    fun subscribeToThingShadow() {
        awsManager.subscribeToTopicShadowAccepted(iotThing) {
            IOTshadowMessageEvent.postValue(it)
        }
    }

    private fun getClientConfigIot() {
            apiManager.execute(IotService.GetClientConfigurationIotBuilder().setCompletion {
                if (it.isSuccess) {
                    if (it.results.status.equals("Success", true)) {
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
                    } else {
                        if (IOT_INTERVAL <= awsManager.getIntervalCount()) {
                            Log.d("elad", "Interval number : " + IOT_INTERVAL)
                            Log.d(
                                "elad",
                                "New Config will be called at : " + awsManager.getIntervalTimeout()
                            )
                            Timer(
                                "IOT Registration Intervals",
                                false
                            ).schedule(awsManager.getIntervalTimeout()) {
                                getClientConfigIot()
                                IOT_INTERVAL++
                            }

                        } else {
                            it.error = APIException()
                            registrationEvent.postValue(Resource.Error(it.error))
                            IOT_INTERVAL = 1
                        }

                    }
                }
            })
        }

    private fun obtainTopicList(results: IotClientConfiguration?): List<KeyValue> {
        return results?.topics as List<KeyValue>
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

    private fun saveIotInfo(iot: IotClientConfiguration) {
        iotThing = getThingName(iot.thingArn)
        iotEndpoint = getATSIotEndpoint(iot.endPoint);
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

fun getMidnight(date : Long) : String {
 val todayMidnightCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

 todayMidnightCalendar.timeInMillis = date * 1000

 todayMidnightCalendar.apply {
     this[Calendar.MILLISECOND] = 0
     this[Calendar.SECOND] = 0
     this[Calendar.MINUTE] = 0
     this[Calendar.HOUR_OF_DAY] = 0
 }

 return (todayMidnightCalendar.timeInMillis/1000).toString()
}

fun callCloudfrontEpgSevice(
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
             var epgList = parseEPGJson(data,channel)
             listener(true, "success",epgList)
         } else {
             listener(false, "Communication Error", emptyList())
         }
     } catch (e: Exception) {
         listener(false, "Json Struct Error", emptyList())
     }
 }.start()
}

fun callCloudfrontLineupSevice(
        url: URL,
        ks: String,
        listener: (status: Boolean, message: String, data: List<ChannelCS>) -> Unit
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
                    var channelList = parseLineupJson(data)
                    listener(true, "success",channelList)
                } else {
                    listener(false, "Communication Error", emptyList())
                }
            } catch (e: Exception) {
                listener(false, "Json Struct Error", emptyList())
            }
        }.start()
    }

    private fun parseEPGJson(jsonString: String, channelID:String) : List<EPGProgram>{
     var result = ArrayList<EPGProgram>()
     try {
         val jsonObject = JsonParser().parse(jsonString) as JsonObject
         val jsonArray = jsonObject.getAsJsonObject("epgChunk").getAsJsonArray(channelID)

         for (i in 0 until jsonArray.size()) {
             val item = jsonArray[i].asJsonObject
//             val item = jsonArray[i].asJsonObject.getAsJsonObject("ProgramAsset")
             val startDate = item.get("startDate").asLong
             val endDate = item.get("endDate").asLong
             val name = item.get("name").asString
             val id = item.get("id").asLong.toString()

             val program = EPGProgram(name,startDate,endDate,id)
             result.add(program)

         }
         if(result.isNotEmpty()) Log.d("TEST",result.size.toString()+" Programs Were Received For Linear Channel "+channelID)

         return result;

     } catch (ex: JsonSyntaxException) {
         return result
     }
    }
    private fun parseLineupJson(jsonString: String) : List<ChannelCS>{
        var result = ArrayList<ChannelCS>()
        try {
            val jsonObject = JsonParser().parse(jsonString) as JsonObject
            val jsonArray = jsonObject.getAsJsonObject("result").getAsJsonArray("objects")

            for (i in 0 until jsonArray.size()) {
                val item = jsonArray[i].asJsonObject
                val lcn = if (item.get("lcn").isJsonNull) 0 else item.get("lcn").asInt
                val name = item.get("name").asString

                val id = item.get("id").asLong.toString()
                val descripion = item.get("description")?.asString

                val channel = ChannelCS(name,id,descripion,lcn)
                result.add(channel)

            }
            if(result.isNotEmpty()) Log.d("TEST",result.size.toString()+" Channels Were Received")
            return result

        } catch (ex: JsonSyntaxException) {
            return result
        }
    }
}