package com.kaltura.kflow.presentation.aws

import com.amazonaws.mobile.client.results.SignInState
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.kaltura.kflow.entity.Iot
import com.kaltura.kflow.entity.IotClientConfiguration
import com.kaltura.kflow.manager.AwsManager
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.service.IotService
import com.kaltura.kflow.utils.*
import org.json.JSONObject

/**
 * Created by alex_lytvynenko on 2020-01-14.
 */
class IotViewModel(private val apiManager: PhoenixApiManager,
                   private val preferenceManager: PreferenceManager,
                   private val awsManager: AwsManager) : BaseViewModel(apiManager) {

    private var announcementTopic = ""
    private var iotThing = preferenceManager.iotThing
    private var iotEndpoint = preferenceManager.iotEndpoint
    private var iotUsername = preferenceManager.iotUsername
    private var iotPassword = preferenceManager.iotPassword

    val registrationEvent = SingleLiveEvent<Resource<SignInState>>()
    val connectEvent = SingleLiveEvent<Resource<AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus>>()
    val shadowMessageEvent = SingleLiveEvent<Resource<String>>()
    val announcementMessageEvent = SingleLiveEvent<Resource<String>>()

    fun register() {
        getClientConfigIot()
    }

    fun initMqtt() {
        awsManager.initMqtt(iotEndpoint)
    }

    fun connect() {
        awsManager.getThingShadow(iotThing) {
            if (it.isSuccess())
                shadowMessageEvent.postValue(it)

            awsManager.connect {
                connectEvent.postValue(it)
            }
        }
    }

    fun subscribeToTopicAnnouncement() {
        awsManager.subscribeToTopic(announcementTopic) {
            announcementMessageEvent.postValue(it)
        }
    }

    fun subscribeToThingShadow() {
        awsManager.subscribeToTopicShadowAccepted(iotThing) {
            shadowMessageEvent.postValue(it)
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
}