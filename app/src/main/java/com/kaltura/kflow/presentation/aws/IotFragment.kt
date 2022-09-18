package com.kaltura.kflow.presentation.aws

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.amazonaws.mobile.client.results.SignInState
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.kaltura.client.types.Epg
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_iot.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class IotFragment : SharedTransitionFragment(R.layout.fragment_iot) {

    //Shadow Messages Keys
    private val KEY_STATE = "state"
    private val KEY_DESIRED = "desired"
    private val KEY_NEW_MESSAGE = "NewMessage"
    private val KEY_HEADER = "header"
    private val KEY_EVENT_TYPE = "event_type"
    private val KEY_IOT_EPG_TYPE = "epg_update"
    private val KEY_IOT_LINEUP_TYPE = "lineup_update"
    private val KEY_LIVE_ASSET_ID = "live_asset_id"

    //Topic Announcments Keys
    private val KEY_ANNONCMENT_TOPIC = "message"

    private val viewModel: IotViewModel by viewModel()
    private var epgUpdates = arrayListOf<Epg>()

    override fun debugView(): DebugView = debugView
    override val feature = Feature.IOT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connect.visibleOrGone(epgUpdates.isNotEmpty())
        register.setOnClickListener {
            showAssets.gone()
            makeRegisterRequest()
        }
        connect.setOnClickListener {
            showAssets.gone()
            viewModel.connect()
        }
        showAssets.navigateOnClick {
            IotFragmentDirections.navigateToAssetList(assets = epgUpdates.toTypedArray())
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.registrationEvent,
                error = { register.error(lifecycleScope) },
                success = {
                    register.success(lifecycleScope)
                    Snackbar.make(requireView(), when (it) {
                        SignInState.DONE -> {
                            connect.visible()
                            viewModel.initMqtt()
                            "AWS Sign-in done."
                        }
                        SignInState.SMS_MFA -> "Please confirm sign-in with SMS."
                        SignInState.NEW_PASSWORD_REQUIRED -> "Please confirm sign-in with new password."
                        else -> "Unsupported sign-in confirmation: $it"
                    }, Snackbar.LENGTH_LONG).show()
                })
        observeResource(viewModel.connectEvent,
                error = {
                    it.printStackTrace()
                    longToast("Mqtt Connection error: $it")
                },
                success = {
                    if (it == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                        longToast("Mqtt Is Connected")
//                        viewModel.subscribeToTopicAnnouncement()
                        viewModel.subscribeToEPGUpdates()
//                        viewModel.subscribeToThingShadow()
                    }
                })
        observeResource(viewModel.IOTshadowMessageEvent) {
            try {
                val jsonObject = JsonParser().parse(it) as JsonObject
                val setPoint = jsonObject.getAsJsonObject(KEY_STATE).getAsJsonObject(KEY_DESIRED)[KEY_NEW_MESSAGE].asString
                longToast(setPoint)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                longToast("Error Parsing Message : $e")
            }
        }
        observeResource(viewModel.IOTepgMessageEvent) {
            try {
                val jsonObject = JsonParser().parse(it) as JsonObject
//                handleEPGEvent(jsonObject)
                handleIOTEvent(jsonObject)
                longToast(jsonObject.toString())
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                longToast("Error Parsing Message : $e")
            }
        }
//        observeResource(viewModel.IOTannouncementMessageEvent,
//                error = {
//                    it.printStackTrace()
//                    longToast("Subscribe to topic error: $it")
//                },
//                success = {
//                    longToast(it)
//                })
//        observeResource(viewModel.epgUpdates,
//                error = {
//                    toast("Failed to fetch EPG updates: ${it.message ?: ""}")
//                },
//                success = {
//                    epgUpdates = it
//                    showAssets.text = getQuantityString(R.plurals.show_updates, epgUpdates.size)
//                    showAssets.visible()
//                })
    }

    private fun makeRegisterRequest() {
        withInternetConnection {
            clearDebugView()

            register.startAnimation {
                viewModel.register()
            }
        }
    }

    private fun handleEventType(jsonString: String) {
        try {
            val jsonObject = JsonParser().parse(jsonString) as JsonObject
            if (jsonObject.has(KEY_HEADER)) {
                val header = jsonObject.getAsJsonObject(KEY_HEADER)
                if (header.has(KEY_EVENT_TYPE)) {
                    val eventType = header.getAsJsonPrimitive(KEY_EVENT_TYPE).asInt
                    if (eventType == 1) {
                        val liveAssetId = jsonObject.getAsJsonPrimitive(KEY_LIVE_ASSET_ID).asLong
                        viewModel.getEpgUpdates(liveAssetId)
                    }
                }
            }
        } catch (ex: JsonSyntaxException) {
        }
    }

    private fun handleIOTEvent(jsonObj: JsonObject) {
        try {

            if (jsonObj.has(KEY_HEADER)) {
                val header = jsonObj.getAsJsonObject(KEY_HEADER)
                if (header.has(KEY_EVENT_TYPE)) {
                    val eventType = header.getAsJsonPrimitive(KEY_EVENT_TYPE).asString
                    if (eventType == KEY_IOT_EPG_TYPE) {
                        val liveAssetId = jsonObj.getAsJsonPrimitive(KEY_LIVE_ASSET_ID).asLong
                        viewModel.getEpgUpdates(liveAssetId)
                    }else if(eventType == KEY_IOT_LINEUP_TYPE){
                        longToast("IOT Received: "+jsonObj)
                    }else{
                        longToast("IOT Received: "+jsonObj)
                    }
                }
            }else{
                longToast("IOT Received: "+jsonObj)
            }
        } catch (ex: JsonSyntaxException) {
        }
    }
}