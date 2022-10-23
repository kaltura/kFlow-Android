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
import com.kaltura.kflow.entity.ChannelCS
import com.kaltura.kflow.entity.EPGProgram
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_iot.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class IotFragment : SharedTransitionFragment(R.layout.fragment_iot) {

    //Shadow Messages Keys
    private val KEY_IOT_EPG_TYPE = "epg_update"
    private val KEY_IOT_LINEUP_TYPE = "lineup_update"
    private val KEY_STATE = "state"
    private val KEY_DESIRED = "desired"
    private val KEY_NEW_MESSAGE = "NewMessage"
    private val KEY_HEADER = "header"
    private val KEY_PROPERTIES = "properties"
    private val KEY_EVENT_TYPE = "event_type"
    private val KEY_EVENT_ENUM = "enum"
    private val KEY_LIVE_ASSET_ID = "live_asset_id"
    private val KEY_LIVE_START_DATE = "start_date"

    //Topic Announcments Keys

    private val viewModel: IotViewModel by viewModel()
    private var epgUpdates = arrayListOf<Epg>()
    private var epgassets = arrayListOf<EPGProgram>()
    private var channelassets = arrayListOf<ChannelCS>()

    override fun debugView(): DebugView = debugView
    override val feature = Feature.IOT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connect.visibleOrGone(epgUpdates.isNotEmpty())
        register.setOnClickListener {
            showEPGAssets.gone()
            showLineupAssets.gone()
            makeRegisterRequest()
        }
        connect.setOnClickListener {
            showEPGAssets.gone()
            showLineupAssets.gone()
            viewModel.connect()
        }

        showEPGAssets.navigateOnClick {
            if (epgassets.isNotEmpty())
                IotFragmentDirections.navigateToAssetListCs(epgassets = epgassets.toTypedArray())
            else
                IotFragmentDirections.navigateToAssetList(assets = epgUpdates.toTypedArray())
        }

        showLineupAssets.navigateOnClick {
            IotFragmentDirections.navigateToChannelListCs(channelassets = channelassets.toTypedArray())
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
                    viewModel.subscribeToAvailableTopics()
//                    viewModel.subscribeToTopicAnnouncement()
//                    viewModel.subscribeToEPGUpdates()
//                    viewModel.subscribeToLineupUpdates()
//                    viewModel.subscribeToThingShadow()
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
//                handleIOTUpdateEvent(jsonObject)
                handleIOTEvent(jsonObject)
                longToast(jsonObject.toString())
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                longToast("Error Parsing Message : $e")
            }
        }
        observeResource(viewModel.IOTannouncementMessageEvent,
            error = {
                it.printStackTrace()
                longToast("Subscribe to topic error: $it")
            },
            success = {
                longToast(it)
            })
//        observeResource(viewModel.IOTLineupMessageEvent) {
//            try {
//                val jsonObject = JsonParser().parse(it) as JsonObject
//                handleIOTUpdateEvent(jsonObject)
//                longToast(jsonObject.toString())
//            } catch (e: JsonSyntaxException) {
//                e.printStackTrace()
//                longToast("Error Parsing Message : $e")
//            }
//        }
//        observeResource(viewModel.IOTannouncementMessageEvent,
//            error = {
//                it.printStackTrace()
//                longToast("Subscribe to topic error: $it")
//            },
//            success = {
//                longToast(it)
//            })
//        observeResource(viewModel.epgUpdates,
//            error = {
//                toast("Failed to fetch EPG updates: ${it.message ?: ""}")
//            },
//            success = {
//                epgUpdates = it
//                showEPGAssets.text = getQuantityString(R.plurals.show_updates, epgUpdates.size)
//                showEPGAssets.visible()
//            })
    }

    private fun makeRegisterRequest() {
        withInternetConnection {
            clearDebugView()

            register.startAnimation {
                viewModel.register()
            }
        }
    }

    private fun handleIOTEvent(jsonObj: JsonObject) {
        try {
            if (jsonObj.has(KEY_HEADER)) {
                val properties = jsonObj.getAsJsonObject(KEY_HEADER).getAsJsonObject(KEY_PROPERTIES)
                if (properties.has(KEY_EVENT_TYPE)) {
                    val eventType = properties.getAsJsonObject(KEY_EVENT_TYPE).getAsJsonArray(KEY_EVENT_ENUM).get(0).asString
                    if (eventType == KEY_IOT_EPG_TYPE) {
                        val liveAssetId = jsonObj.getAsJsonPrimitive(KEY_LIVE_ASSET_ID).asLong
                        val date = viewModel.getMidnight(jsonObj.getAsJsonPrimitive(KEY_LIVE_START_DATE).asLong)
                        val ks = viewModel.getKs() as String
                        val parthnerID = viewModel.getParthnerID()
                        val url = URL(viewModel.getCloudfrontUrl()+"epg/action/get/partnerid/"+parthnerID+"/date/"+date+"/slots/all?channels="+liveAssetId)

                        viewModel.callCloudfrontEpgSevice(url,ks,liveAssetId.toString()){ status,message,data ->
                            when (status) {
                                true -> {
                                    if (!data.isEmpty()){
                                        requireActivity().runOnUiThread {
                                            epgassets = data as ArrayList<EPGProgram>
                                            showEPGAssets.text = getQuantityString(R.plurals.show_assets, epgassets.size)
                                            showEPGAssets.visible()
                                        }
                                    }
                                }
                                else -> {
                                    requireActivity().runOnUiThread { longToast(message) }
                                }
                            }
                        }
                    }else if(eventType == KEY_IOT_LINEUP_TYPE){

                        val url = URL(viewModel.getCloudfrontUrl()+"lineup/action/get?pageSize=1600")
                        val ks = viewModel.getKs() as String
                        viewModel.callCloudfrontLineupSevice(url,ks){ status,message,data ->
                            when (status) {
                                true -> {
                                    if (!data.isEmpty()){
                                        requireActivity().runOnUiThread {
                                            channelassets = data as ArrayList<ChannelCS>
                                            showLineupAssets.text = getQuantityString(R.plurals.show_assets, channelassets.size)
                                            showLineupAssets.visible()
                                        }
                                    }
                                }
                                else -> {
                                    requireActivity().runOnUiThread { longToast(message) }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ex: JsonSyntaxException) {
        }
    }
}