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
import com.kaltura.kflow.entity.EPGProgram
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_iot.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class IotFragment : SharedTransitionFragment(R.layout.fragment_iot) {

    //Shadow Messages Keys
    private val KEY_STATE = "state"
    private val KEY_DESIRED = "desired"
    private val KEY_NEW_MESSAGE = "NewMessage"
    private val KEY_HEADER = "header"
    private val KEY_EVENT_TYPE = "messageType"
    private val KEY_EPG_UPDATE_TYPE = "EPGUpdateIoTNotification"
    private val KEY_LIVE_ASSET_ID = "live_asset_id"

    //Topic Announcments Keys
    private val KEY_ANNONCMENT_TOPIC = "message"

    private val viewModel: IotViewModel by viewModel()
    private var epgUpdates = arrayListOf<Epg>()
    private var epgassets = arrayListOf<EPGProgram>()

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
        temp_epg.setOnClickListener {
            val url = URL("https://cache.prd1.ott.kaltura.com/api_v3/service/epg/action/get/date/"+viewModel.getMidnightDate(Calendar.getInstance().timeInMillis)+"/slots/all?channels=2072065")
            val ks = viewModel.getKs()

            viewModel.callCloudfront(url,ks!!,"2072065") { status,message,data ->
                when (status) {
                    true -> {
                        if (!data.isEmpty()){
                                requireActivity().runOnUiThread {
                                    epgassets = data as ArrayList<EPGProgram>
                                    showAssets.text = getQuantityString(R.plurals.show_assets, epgassets.size)
                                    showAssets.visible()
                                }
                        }
                    }
                    else -> {
                        requireActivity().runOnUiThread { longToast(message) }
                    }
                }

            }
        }
        showAssets.navigateOnClick {
            if (epgassets.isNotEmpty())
                IotFragmentDirections.navigateToAssetListCs(epgassets = epgassets.toTypedArray())
            else
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
                    viewModel.subscribeToTopicAnnouncement()
                    viewModel.subscribeToThingShadow()
                }
            })
        observeResource(viewModel.shadowMessageEvent) {
            try {
                val jsonObject = JsonParser().parse(it) as JsonObject
                val setPoint = jsonObject.getAsJsonObject(KEY_STATE).getAsJsonObject(KEY_DESIRED)[KEY_NEW_MESSAGE].asString
                handleEventType(setPoint)
                longToast(setPoint)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                longToast("Error Parsing Message : $e")
            }
        }
        observeResource(viewModel.announcementMessageEvent,
            error = {
                it.printStackTrace()
                longToast("Subscribe to topic error: $it")
            },
            success = {
                longToast(it)
            })
        observeResource(viewModel.epgUpdates,
            error = {
                toast("Failed to fetch EPG updates: ${it.message ?: ""}")
            },
            success = {
                epgUpdates = it
                showAssets.text = getQuantityString(R.plurals.show_updates, epgUpdates.size)
                showAssets.visible()
            })
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
                    val messageType = header.getAsJsonPrimitive(KEY_EVENT_TYPE).asString
                    if (messageType.equals(KEY_EPG_UPDATE_TYPE,true)) {
                        val liveAssetId = jsonObject.getAsJsonPrimitive(KEY_LIVE_ASSET_ID).asLong
                        viewModel.getEpgUpdates(liveAssetId)
                    }
                }
            }
        } catch (ex: JsonSyntaxException) {
        }
    }


}